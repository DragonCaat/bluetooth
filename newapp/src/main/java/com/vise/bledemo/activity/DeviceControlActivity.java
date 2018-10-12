package com.vise.bledemo.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.vise.baseble.ViseBle;
import com.vise.baseble.common.ConnectState;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.resolver.GattAttributeResolver;
import com.vise.baseble.utils.HexUtil;
import com.vise.bledemo.R;
import com.vise.bledemo.adapter.GattServiceAdapter;
import com.vise.bledemo.adapter.OutPutAdapter;
import com.vise.bledemo.adapter.ShowCommandAdapter;
import com.vise.bledemo.bean.Constant;
import com.vise.bledemo.common.BluetoothDeviceManager;
import com.vise.bledemo.common.KeyCodeUtils;
import com.vise.bledemo.common.PreferencesUtils;
import com.vise.bledemo.common.SampleGattAttributes;
import com.vise.bledemo.common.ToastUtil;
import com.vise.bledemo.database.RecordDatabaseUtils;
import com.vise.bledemo.event.CallbackDataEvent;
import com.vise.bledemo.event.ConnectEvent;
import com.vise.bledemo.event.NotifyDataEvent;
import com.vise.xsnow.cache.SpCache;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;

/**
 * 设备数据操作相关展示界面
 *
 * @author Darcy
 */
public class DeviceControlActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";
    private final String PROPERTY = "PROPERTY";

    public static final String WRITE_CHARACTERISTI_UUID_KEY = "write_uuid_key";
    public static final String NOTIFY_CHARACTERISTIC_UUID_KEY = "notify_uuid_key";
    public static final String WRITE_DATA_KEY = "write_data_key";
    //private SimpleExpandableListAdapter simpleExpandableListAdapter;
    //展示服务的适配器
    private GattServiceAdapter gattServiceAdapter;
    //@BindView(R.id.connection_state)
    TextView mConnectionState;
    // @BindView(R.id.uuid)
    TextView mGattUUID;
    //@BindView(R.id.description)
    TextView mGattUUIDDesc;
    // @BindView(R.id.data_as_string)
    TextView mDataAsString;
    // @BindView(R.id.data_as_array)
    TextView mDataAsArray;
    // TextView mTvDeviceName;
    private TextView mInput;
    //显示文本的输入内容
    private EditText mOutput;
    private RecyclerView mRvShowOut;
    //用于存放输出数据的list
    private List<String> list = new ArrayList<>();
    //输出数据的适配器
    private OutPutAdapter adapter;
    private Button mBtnClean;
    //展示连接框
    private ProgressDialog progressDialog;
    private SpCache mSpCache;
    //设备信息
    private BluetoothLeDevice mDevice;
    //输出数据展示
    private StringBuilder mOutputInfo = new StringBuilder();
    private List<BluetoothGattService> mGattServices = new ArrayList<>();
    //设备特征值集合
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    //判断用户是否连接上的变量
    private boolean isConnection = false;
    private Context mContext;
    //选择可写的特征
    public TextView mEtWriteCharacteristic;
    //选择可通知的特征
    public TextView mEtNotifyCharacteristic;
    private Button mBtnOutPutTxt;
    //选择输入的格式
    private Spinner mSpinner;
    //自动完成的输入框
    // private AutoCompleteTextView autoCompleteTextView;

    //保存的命令的适配器和数据源
    private ShowCommandAdapter commandAdapter;
    private List<String> queryData = new ArrayList<>();

    private Button mBtnShowCommand;
    private int INPUT_TYPE = 0; //0:文本 1：十六进制
    private int OUT_TYPE = 0; //0:文本  1：十六进制

    //作为一个全局单例，管理所有BLE设备的连接
    public static BluetoothClient mClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        // ButterKnife.bind(this);
        //注册事件管理的监听
        BusManager.getBus().register(this);
        mContext = this;
        init();
        if (mClient == null)
            mClient = new BluetoothClient(mContext);
    }

    /**
     * 初始化控件
     */
    private void init() {
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mGattUUID = (TextView) findViewById(R.id.uuid);
        mGattUUIDDesc = (TextView) findViewById(R.id.description);
        mDataAsString = (TextView) findViewById(R.id.data_as_string);
        mDataAsArray = (TextView) findViewById(R.id.data_as_array);
        //输入框
        mInput = (TextView) findViewById(R.id.input);
        mOutput = (EditText) findViewById(R.id.output);
        // mTvDeviceName = (TextView) findViewById(R.id.device_name);
        mEtWriteCharacteristic = (TextView) findViewById(R.id.show_write_characteristic);
        mEtWriteCharacteristic.setOnClickListener(this);
        mEtNotifyCharacteristic = (TextView) findViewById(R.id.show_notify_characteristic);
        mEtNotifyCharacteristic.setOnClickListener(this);
        mBtnOutPutTxt = (Button) findViewById(R.id.output_txt);
        mBtnOutPutTxt.setOnClickListener(this);
        mBtnClean = (Button) findViewById(R.id.btn_clean);
        mBtnClean.setOnClickListener(this);


        mBtnShowCommand = (Button) findViewById(R.id.btn_show_command);
        mBtnShowCommand.setOnClickListener(this);

        mSpinner = (Spinner) findViewById(R.id.spinner);
        final int output_flag = PreferencesUtils.getInt(mContext, Constant.OUTPUT_FLAG, 0);
        if (output_flag == 0)
            mSpinner.setSelection(0);
        else
            mSpinner.setSelection(1);
        //输出格式的监听
        mSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                OUT_TYPE = arg2;
                PreferencesUtils.putInt(mContext, Constant.OUTPUT_FLAG, arg2);

                //将文本转化问十六进制形式
                if (OUT_TYPE == 0) {//文本形式
                    for (int i = 0; i < list.size(); i++) {
                        String s = list.get(i);
                        if (isHexData(s)) {
                            s = hexStringToString(s);
                            list.remove(i);
                            list.add(i, s);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    //将十六进制的文本转化成字符串形式
                } else if (OUT_TYPE == 1) {//十六进制形式
                    for (int i = 0; i < list.size(); i++) {
                        String s = list.get(i);
                        if (!isHexData(s)) {
                            s = str2HexStr(s);
                            list.remove(i);
                            list.add(i, s);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
//        List<String> queryData = RecordDatabaseUtils.queryData(DeviceControlActivity.this);
//        String[]autoString = new String[0];
//        if (queryData!=null&&queryData.size()>0){
//            for (int j = 0;j<queryData.size();j++){
//                autoString = new String[queryData.size()];
//                autoString[j]=queryData.get(j);
//            }
//        }

        //自动输入框
        //autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
//        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
//                android.R.layout.simple_dropdown_item_1line, autoString);
        //showCommandAdapter = new ShowCommandAdapter(queryData);
        //autoCompleteTextView.setAdapter(adapter1);

        //展示输出的recycleView
        mRvShowOut = (RecyclerView) findViewById(R.id.rv_show_out);
        GridLayoutManager manager = new GridLayoutManager(this, 1);
        mRvShowOut.setLayoutManager(manager);
        adapter = new OutPutAdapter(list, this);
        mRvShowOut.setAdapter(adapter);

        //获取上一个activity传递过来的数据
        mDevice = getIntent().getParcelableExtra(DeviceDetailActivity.EXTRA_DEVICE);
        if (mDevice != null) {
            ((TextView) findViewById(R.id.device_address)).setText(mDevice.getAddress());
            ((TextView) findViewById(R.id.device_name)).setText(mDevice.getName());
        }
        mSpCache = new SpCache(this);



        //选择可读服务
        findViewById(R.id.select_read_characteristic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnection)
                    showGattServices();
                else
                    ToastUtil.showShortToast(mContext, "当前设备未连接");
            }
        });

        //发送指令
        final Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String readCharacter = mEtWriteCharacteristic.getText().toString();
                String notifyCharacter = mEtNotifyCharacteristic.getText().toString();
                if ("点击选择可读服务".equals(readCharacter) || "点击选择可通知服务".equals(notifyCharacter)) {
                    ToastUtil.showShortToast(mContext, "请先选择character");
                    return;
                }
                String userInputStr = mInput.getText().toString();
                //16进制的指令
                String commond = "";
                if ("点击输入命令".equals(userInputStr)) {
                    ToastUtil.showToast(DeviceControlActivity.this, "请输入指令");
                    return;
                }

                //如果不是16进制的，则转化为16进制的输入
                if (!isHexData(mInput.getText().toString())) {
                    //ToastUtil.showToast(DeviceControlActivity.this, "Please input hex data command!");
                    String s = str2HexStr(mInput.getText().toString());
                    commond = s;
                } else {
                    commond = userInputStr;
                }
                mSpCache.put(WRITE_DATA_KEY + mDevice.getAddress(), userInputStr);
                BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(commond));
                KeyCodeUtils.closeKeyCode(mContext, send);
            }
        });

        //点击命令的事件
        mInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String readCharacter = mEtWriteCharacteristic.getText().toString();
                String notifyCharacter = mEtNotifyCharacteristic.getText().toString();
                if ("点击选择可读服务".equals(readCharacter) || "点击选择可通知服务".equals(notifyCharacter)) {
                    ToastUtil.showShortToast(mContext, "请先选择character");
                    return;
                }
                showDialog();
            }
        });

    }

    //展示输入的dialog
    private void showDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext, R.style.Theme_AppCompat_Light_Dialog_Alert);
        final android.support.v7.app.AlertDialog dialog = builder.create();
        View view = View.inflate(mContext, R.layout.dialog_input_command, null);
        dialog.setView(view, 0, 0, 0, 0);
        final EditText editText = (EditText) view.findViewById(R.id.et_user_input);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        int anInt = PreferencesUtils.getInt(mContext, Constant.INPUT_FLAG, 0);
        //Log.i("hello", "showDialog: "+anInt);
        if (anInt == 0)
            spinner.setSelection(0);
        else
            spinner.setSelection(1);

        Button btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String trim = editText.getText().toString().trim();
                if (TextUtils.isEmpty(trim))
                    ToastUtil.showShortToast(mContext, "命令不能为空");
                else {
                    if (RecordDatabaseUtils.hasData(trim, mContext))
                        ToastUtil.showShortToast(mContext, "命令已存在");
                    else {
                        RecordDatabaseUtils.insertData(trim, mContext);
                        ToastUtil.showShortToast(mContext, "保存成功");
                        if (commandAdapter == null)
                            commandAdapter = new ShowCommandAdapter(queryData);
                        queryData.add(trim);
                        commandAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                INPUT_TYPE = arg2;
                PreferencesUtils.putInt(mContext, Constant.INPUT_FLAG, arg2);
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        final Button btnOk = (Button) view.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInputStr = editText.getText().toString();
                //16进制的指令
                String command;
                if (TextUtils.isEmpty(userInputStr)) {
                    ToastUtil.showToast(DeviceControlActivity.this, "请输入指令");
                    return;
                }
                if (!isHexData(userInputStr) && INPUT_TYPE == 1) {
                    ToastUtil.showShortToast(mContext, "命令不符合要求");
                    return;
                } else if (isHexData(userInputStr) && INPUT_TYPE == 0) {
                    ToastUtil.showShortToast(mContext, "命令不符合要求");
                    return;
                }
                //文本
                if (INPUT_TYPE == 0) {
                    String s = str2HexStr(editText.getText().toString());
                    command = s;
                } else {
                    command = userInputStr;
                }
                mSpCache.put(WRITE_DATA_KEY + mDevice.getAddress(), userInputStr);
                BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(command));
                dialog.dismiss();
                KeyCodeUtils.closeKeyCode(mContext, btnOk);
                //展示用户输入的命令
                mInput.setText(userInputStr);
            }
        });
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ListView listView = view.findViewById(R.id.lv);
        //从数据库中获取保存的命令数据
        queryData = RecordDatabaseUtils.queryData(DeviceControlActivity.this);
        if (queryData.size() > 0) {
            commandAdapter = new ShowCommandAdapter(queryData);
        }

        listView.setAdapter(commandAdapter);
        //条目点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String itemString = queryData.get(i);
                mInput.setText(itemString);
                String commond;
                if (!isHexData(itemString)) {
                    String s = str2HexStr(itemString);
                    commond = s;
                } else {
                    commond = itemString;
                }
                mSpCache.put(WRITE_DATA_KEY + mDevice.getAddress(), itemString);
                BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(commond));
                // writeBytes(surviceUuid,characterUuid,itemString);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * 连接成功的时候调用
     *
     * @param event 连接的监听回调
     */
    @Subscribe
    public void showConnectedDevice(ConnectEvent event) {
        if (event != null) {
            if (event.isSuccess()) {
                hideProgressDialog();
                isConnection = true;
                ToastUtil.showToast(DeviceControlActivity.this, "连接成功!");
                mConnectionState.setText("已连接");
                mConnectionState.setTextColor(Color.GREEN);
                invalidateOptionsMenu();
                if (event.getDeviceMirror() != null && event.getDeviceMirror().getBluetoothGatt() != null) {
                    gattServiceAdapter = displayGattServices(event.getDeviceMirror().getBluetoothGatt().getServices());
                }
            } else {
                if (event.isDisconnected()) {
                    isConnection = false;
                    isConnection = false;
                    ToastUtil.showToast(DeviceControlActivity.this, "已断开连接");
                } else {
                    hideProgressDialog();
                    ToastUtil.showToast(DeviceControlActivity.this, "连接失败");
                }
                mConnectionState.setText("未连接");
                mConnectionState.setTextColor(Color.RED);
                invalidateOptionsMenu();
                //clearUI();
                isConnection = false;
            }
        }
    }

    /***
     * 对默认信息的监听回调
     * 展示硬件信息
     * */
    @Subscribe
    public void showDeviceCallbackData(CallbackDataEvent event) {
        if (event != null) {
            if (event.isSuccess()) {
                if (event.getBluetoothGattChannel() != null && event.getBluetoothGattChannel().getCharacteristic() != null
                        && event.getBluetoothGattChannel().getPropertyType() == PropertyType.PROPERTY_READ) {

                    showReadInfo(event.getBluetoothGattChannel().getCharacteristic().getUuid().toString(), event.getData());
                }
            } else {
                mEtWriteCharacteristic.setText("点击选择可读服务");

                mEtNotifyCharacteristic.setText("点击选择可通知服务");
            }
        }
    }

    /**
     * 展示通知的数据
     */
    @SuppressLint("SetTextI18n")
    @Subscribe
    public void showDeviceNotifyData(NotifyDataEvent event) {
        //Log.i("hello", "showDeviceNotifyData: 通知" + HexUtil.encodeHexStr(event.getData()));
        if (event != null && event.getData() != null && event.getBluetoothLeDevice() != null
                && event.getBluetoothLeDevice().getAddress().equals(mDevice.getAddress())) {
            Log.i("hello", "showDeviceNotifyData: 新数据" + HexUtil.encodeHexStr(event.getData()));

            mOutputInfo.append(HexUtil.encodeHexStr(event.getData())).append("\n");
            mOutput.setText(" " + mOutputInfo.toString());

            if (OUT_TYPE == 0) {//文本输出
                String s = HexUtil.encodeHexStr(event.getData());
                String s1 = hexStringToString(s);
                list.add(0, s1);
                adapter.notifyDataSetChanged();
            } else if (OUT_TYPE == 1) {//十六进制输出
                String s = HexUtil.encodeHexStr(event.getData());
                list.add(0, s);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
            BluetoothDeviceManager.getInstance().connect(mDevice);
            showProgressDialog("正在连接...");
            invalidateOptionsMenu();
        }

    }

    @Override
    protected void onResume() {
        invalidateOptionsMenu();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.connect, menu);
        if (BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            mConnectionState.setText("已连接");
            mConnectionState.setTextColor(Color.GREEN);
            DeviceMirror deviceMirror = ViseBle.getInstance().getDeviceMirror(mDevice);
            if (deviceMirror != null) {
                gattServiceAdapter = displayGattServices(deviceMirror.getBluetoothGatt().getServices());
            }
            showDefaultInfo();
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            mConnectionState.setText("未连接");
            mConnectionState.setTextColor(Color.RED);
            clearUI();
        }
        if (ViseBle.getInstance().getConnectState(mDevice) == ConnectState.CONNECT_PROCESS) {
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_progress_indeterminate);
        } else {
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect://连接设备
                if (!BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
                    BluetoothDeviceManager.getInstance().connect(mDevice);
                    showProgressDialog("正在连接...");
                    invalidateOptionsMenu();
                }
                break;
            case R.id.menu_disconnect://断开设备
                if (BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
                    BluetoothDeviceManager.getInstance().disconnect(mDevice);
                    invalidateOptionsMenu();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        BusManager.getBus().unregister(this);
        super.onDestroy();
    }

    /**
     * 根据GATT服务显示该服务下的所有特征值
     *
     * @param gattServices GATT服务
     * @return
     */
    private GattServiceAdapter displayGattServices(
            final List<BluetoothGattService> gattServices) {
        if (gattServices == null) return null;
        String uuid;
        final String unknownServiceString = getResources().getString(R.string.unknown_service);
        final String unknownCharaString = getResources().getString(R.string.unknown_characteristic);

        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, Object>>> gattCharacteristicData
                = new ArrayList<>();

        mGattServices = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        //获取可操作的蓝牙服务
        for (final BluetoothGattService gattService : gattServices) {
            final HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, Object>> gattCharacteristicGroupData =
                    new ArrayList<>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<>();

            // Loops through available Characteristics.
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);

                final HashMap<String, Object> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                //gattCharacteristicGroupData.add(currentCharaData);

                int properties = gattCharacteristic.getProperties();

                final byte[] data = gattCharacteristic.getValue();
                String s = "";
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));

                    try {
                        s = new String(data, "UTF-8") + "\n" + stringBuilder.toString();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                currentCharaData.put(PROPERTY, properties);
                currentCharaData.put("characteristic", gattCharacteristic);
                currentCharaData.put("value", s);

                gattCharacteristicGroupData.add(currentCharaData);
            }

            mGattServices.add(gattService);

            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        GattServiceAdapter adapter = new GattServiceAdapter(gattServiceData, gattCharacteristicData, this);
        return adapter;
    }

    //显示读取到的数据
    private void showReadInfo(String uuid, byte[] dataArr) {
        mGattUUID.setText(uuid != null ? uuid : getString(R.string.no_data));
        mGattUUIDDesc.setText(GattAttributeResolver.getAttributeName(uuid, getString(R.string.unknown)));
        mDataAsArray.setText(HexUtil.encodeHexStr(dataArr));
        mDataAsString.setText(new String(dataArr));
    }

    //展示可读默认的信息
    private void showDefaultInfo() {
        mGattUUID.setText(R.string.no_data);
        mGattUUIDDesc.setText(R.string.no_data);
        mDataAsArray.setText(R.string.no_data);
        mDataAsString.setText(R.string.no_data);
        mInput.setText(mSpCache.get(WRITE_DATA_KEY + mDevice.getAddress(), "点击输入命令"));
        mOutput.setText("");

        mEtWriteCharacteristic.setText(mSpCache.get(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress(), "点击选择可读服务"));
        mEtNotifyCharacteristic.setText(mSpCache.get(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), "点击选择可通知服务"));
        mOutputInfo = new StringBuilder();
    }

    //清空Ui界面
    private void clearUI() {
        mGattUUID.setText(R.string.no_data);
        mGattUUIDDesc.setText(R.string.no_data);
        mDataAsArray.setText(R.string.no_data);
        mDataAsString.setText(R.string.no_data);
        mInput.setText("点击输入命令");
        mOutput.setText("");

        mEtWriteCharacteristic.setText("点击选择可读服务");
        mEtNotifyCharacteristic.setText("点击选择可通知服务");

        mOutputInfo = new StringBuilder();
        gattServiceAdapter = null;

        mSpCache.remove(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress());
        mSpCache.remove(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress());
        mSpCache.remove(WRITE_DATA_KEY + mDevice.getAddress());


        list.clear();
        adapter.notifyDataSetChanged();
    }

    /**
     * 显示GATT服务展示的信息
     */
    private void showGattServices() {
        if (gattServiceAdapter == null) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(DeviceControlActivity.this);
        View view = LayoutInflater.from(DeviceControlActivity.this).inflate(R.layout.item_gatt_services, null);
        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.dialog_gatt_services_list);
        expandableListView.setAdapter(gattServiceAdapter);

        builder.setView(view);
        final AlertDialog dialog = builder.show();
        Window dialogWindow = dialog.getWindow();
        WindowManager m = getWindowManager();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        lp.height = (int) (d.getHeight() * 0.7); // 高度设置为屏幕的0.6

        dialog.setCanceledOnTouchOutside(true);
        //单个的条目点击事件
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                dialog.dismiss();
                final BluetoothGattService service = mGattServices.get(groupPosition);
                final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperties();
                //点击的是可写的characteristic
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    mSpCache.put(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
                    mEtWriteCharacteristic.setText(characteristic.getUuid().toString());
                    BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid(), null);
                    //点击可读的
                } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_READ, service.getUuid(), characteristic.getUuid(), null);
                    BluetoothDeviceManager.getInstance().read(mDevice);
                }
                //如果点击的是通知
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
                    mEtNotifyCharacteristic.setText(characteristic.getUuid().toString());
                    BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_NOTIFY, service.getUuid(), characteristic.getUuid(), null);
                    BluetoothDeviceManager.getInstance().registerNotify(mDevice, false);

                    // notify1(mDevice,(BleGattService)service.getUuid(), characteristic.getUuid());
                    //点击的条目是INDICATE
                } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                    mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
                    mEtNotifyCharacteristic.setText(characteristic.getUuid().toString());
                    BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_INDICATE, service.getUuid(), characteristic.getUuid(), null);
                    BluetoothDeviceManager.getInstance().registerNotify(mDevice, true);
                }
                return true;
            }
        });
    }


    private void writeBytes(UUID serviceUUID, UUID characterUUID, String userInput) {
        String userCommond;
        if (TextUtils.isEmpty(userInput))
            Toast.makeText(mContext, "命令不能为空", Toast.LENGTH_SHORT).show();

        else {
            //如果不是16进制的，则转化为16进制的输入
            if (!isHexData(userInput)) {
                //ToastUtil.showToast(DeviceControlActivity.this, "Please input hex data command!");
                String s = str2HexStr(userInput);
                userCommond = s;
            } else {
                userCommond = userInput;
            }

            byte[] bytes = hexStringToByte(userCommond);
            this.writeCharacteristic(serviceUUID, characterUUID, bytes);
        }
    }

    //写入可写的Characteristic
    private void writeCharacteristic(UUID serviceUUID, UUID characterUUID, byte[] bytes) {
        mClient.write(mDevice.getAddress(), serviceUUID, characterUUID, bytes, new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) {

                    Toast.makeText(mContext, "写入成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //写入可通知的Characteristic
    private void notify1(BluetoothLeDevice device, UUID serviceUUID, UUID characterUUID) {

        Log.i("hello", "notify1: " + device.getAddress());

        mClient.notify(device.getAddress(), serviceUUID, characterUUID, new BleNotifyResponse() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                Log.i("hello", "onNotify: 有新消息哦" + bytesToHexString(value));
                // String s = hexStringToString(bytesToHexString(value));

//                list.add(s);
//                adapter.notifyDataSetChanged();
            }

            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) {
                    Toast.makeText(mContext, "通知已打开", Toast.LENGTH_SHORT).show();
                }

                Log.i("hello", "onResponse: " + code);
            }
        });

    }

    /**
     * 把16进制字符串转换成字节数组
     *
     * @param hex
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 判断是否是16进制
     *
     * @param str 字符串
     */
    private boolean isHexData(String str) {
        if (str == null) {
            return false;
        }
        char[] chars = str.toCharArray();
        if ((chars.length & 1) != 0) {//个数为奇数，直接返回false
            return false;
        }
        for (char ch : chars) {
            if (ch >= '0' && ch <= '9') continue;
            if (ch >= 'A' && ch <= 'F') continue;
            if (ch >= 'a' && ch <= 'f') continue;
            return false;
        }
        return true;
    }

    /**
     * 16进制直接转换成为字符串(无需Unicode解码)
     *
     * @param hexStr
     * @return
     */
    public String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * 16进制转换成为string类型字符串
     *
     * @param s
     * @return
     */
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "UTF-8");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }


    /**
     * 数组转换成十六进制字符串
     *
     * @param bArray
     * @return HexString
     */
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 字符串转换成为16进制(无需Unicode编码)
     *
     * @param str
     * @return
     */
    public String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            // sb.append(' ');
        }
        return sb.toString().trim();
    }

    //展示搜索框
    private void showProgressDialog(String s) {
        if (progressDialog != null)
            progressDialog = null;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(s);
        progressDialog.show();
        //当dialog被dismiss的时候停止搜索
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //如果正在搜索则取消搜索

            }
        });

        progressDialog.setCanceledOnTouchOutside(true);
    }

    //隐藏搜索框
    private void hideProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    /**
     * 点击事件的监听
     *
     * @param view 点击的控件
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //选择可选的服务
            case R.id.show_write_characteristic:
                if (isConnection)
                    showGattServices();
                else
                    ToastUtil.showShortToast(mContext, "当前设备未连接");
                break;

            //选择可通知的服务
            case R.id.show_notify_characteristic:
                if (isConnection)
                    showGattServices();
                else
                    ToastUtil.showShortToast(mContext, "当前设备未连接");
                break;

            //导出文件
            case R.id.output_txt:
                getPermission();
                break;

            //清空文本
            case R.id.btn_clean:
                if (list != null)
                    list.clear();
                adapter.notifyDataSetChanged();
                break;

            //展示保存过的命令只取出8条
            case R.id.btn_show_command:
                List<String> list = new ArrayList<>();
                List<String> queryData = RecordDatabaseUtils.queryData(DeviceControlActivity.this);
                if (queryData.size() > 0 && queryData.size() < 9)
                    initPopupWindow(mBtnShowCommand, queryData);
                else if (queryData.size() > 8) {
                    for (int i = queryData.size() - 9; i < queryData.size(); i++) {
                        list.add(queryData.get(i));
                    }
                    initPopupWindow(mBtnShowCommand, list);
                } else
                    ToastUtil.showShortToast(mContext, "当前设备没有保存命令");
                break;

            default:
                break;
        }
    }


    /**
     * 初始化popWindow界面
     *
     * @param v         popwindow 展示下面的控件
     * @param queryData 用户保存命令的数据源
     */
    private void initPopupWindow(View v, final List<String> queryData) {
        View view = View.inflate(getApplicationContext(), R.layout.pop_window_item,
                null);

        ListView lvShow = (ListView) view.findViewById(R.id.lv_show);
        commandAdapter = new ShowCommandAdapter(queryData);
        lvShow.setAdapter(commandAdapter);

        // 初始化弹出窗体,设定大小
        final PopupWindow popupWindow = new PopupWindow(view, 400, 220);
        //单个条目的点击事件
        lvShow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String itemString = queryData.get(i);
                mInput.setText(itemString);
                String commond;
                if (!isHexData(itemString)) {
                    String s = str2HexStr(itemString);
                    commond = s;
                } else {
                    commond = itemString;
                }
                mSpCache.put(WRITE_DATA_KEY + mDevice.getAddress(), itemString);
                BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(commond));

                popupWindow.dismiss();

            }
        });

        // 设置参数
        // 设置焦点，保证里面的组件可以点击
        popupWindow.setFocusable(true);
        // 设置背景，好处：1、外部点击生效 2、可以播放动画
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));// 透明
        popupWindow.setOutsideTouchable(true);

        // 为显自然，加个渐变动画
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0);
        scaleAnimation.setDuration(400);
        //控制显示的位置
        popupWindow.showAsDropDown(v, 45, 10);
        //显示动画
        view.startAnimation(scaleAnimation);
    }


    //权限的获取
    private void getPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            } else {
//                String outPutStr = mOutput.getText().toString();
//                if (TextUtils.isEmpty(outPutStr))
//                    ToastUtil.showShortToast(mContext, "当前无输出数据");
//                else {
//                    initFile(outPutStr);

                String outPutStr = null;
                for (int j = 0; j < list.size(); j++) {
                    outPutStr = outPutStr + list.get(j);
                }
                initFile(outPutStr);
                showScanSelect();
                // ToastUtil.showShortToast(mContext, "导出成功");
            }

        }
    }

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                //Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
                if (grantResults[i] == 0) {
                    String outPutStr = null;
                    for (int j = 0; j < list.size(); j++) {
                        outPutStr = outPutStr + list.get(j);
                    }

                    initFile(outPutStr);
                    //ToastUtil.showShortToast(mContext, "导出成功");
                    showScanSelect();
                }
            }
        }
    }


    /**
     * 初始化文件夹
     *
     * @param content 要写入的文件内容
     */
    private void initFile(String content) {
        @SuppressLint("SdCardPath") String filePath = "/sdcard/Akahakeji/";

        @SuppressLint
                ("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        String format = simpleDateFormat.format(date);
        String fileName = format + ".txt";

        writeTxtToFile(content, filePath, fileName);
    }

    /**
     * 将字符串写入到文本文件中
     *
     * @param strcontent 要写入的字符串
     * @param filePath   文件的路径
     * @param fileName   文件的名字
     */
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                //Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            // Log.e("TestFile", "Error on write File:" + e);
        }
    }

    /**
     * 生成文件
     *
     * @param fileName 文件名称
     * @param filePath 文件路径
     */
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 生成文件夹
     *
     * @param filePath 文件夹的路径
     */
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            //Log.i("error:", e+"");
        }
    }

    /**
     * 打开指定文件夹
     *
     * @param path 文件夹的路径
     */
    private void openAssignFolder(String path) {
        File file = new File(path);
        if (null == file || !file.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setDataAndType(Uri.fromFile(file), "file/*");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }


    //弹出是否打开文件夹的弹出框
    private void showScanSelect() {
        @SuppressLint("SdCardPath") final String filePath = "/sdcard/Akahakeji/";
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext, R.style.Theme_AppCompat_Light_Dialog_Alert);
        final android.support.v7.app.AlertDialog dialog = builder.create();

        View view = View.inflate(mContext, R.layout.dialog_open_file, null);
        dialog.setView(view, 0, 0, 0, 0);

        Button btnOk = (Button) view.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAssignFolder(filePath);
            }
        });
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
