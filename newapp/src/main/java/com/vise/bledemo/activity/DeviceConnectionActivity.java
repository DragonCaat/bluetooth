package com.vise.bledemo.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.os.Handler;
import android.provider.FontsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
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
import com.vise.bledemo.common.OnCommandClickListener;
import com.vise.bledemo.common.PreferencesUtils;
import com.vise.bledemo.common.SampleGattAttributes;
import com.vise.bledemo.common.ToastUtil;
import com.vise.bledemo.database.RecordDatabaseUtils;
import com.vise.bledemo.event.CallbackDataEvent;
import com.vise.bledemo.event.NotifyDataEvent;
import com.vise.bledemo.view.SlidingLayout;
import com.vise.xsnow.cache.SpCache;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

/***

 * 另一种连接方式
 *
 * */
public class DeviceConnectionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";
    private final String PROPERTY = "PROPERTY";

    public static final String WRITE_DATA_KEY = "write_data_key";

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
    private Dialog progressDialog;
    private SpCache mSpCache;
    //输出数据展示
    private StringBuilder mOutputInfo = new StringBuilder();

    private List<BleGattService> mGattServices = new ArrayList<>();
    //设备特征值集合
    private List<List<BleGattCharacter>> mGattCharacteristics = new ArrayList<>();

    //判断用户是否连接上的变量
    private boolean isConnection = false;

    private Context mContext;
    //选择可写的特征
    public TextView mEtWriteCharacteristic;
    //选择可通知的特征
    public TextView mEtNotifyCharacteristic;
    //选择可读的特征
    public TextView mEtReadCharactertistic;

    private Button mBtnRecycleSend;

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

    public static String macStr;
    private String macName;
    //作为一个全局单例，管理所有BLE设备的连接
    public static BluetoothClient mClient;

    private UUID writeUuid;
    private UUID serviceUuid;

    private Button send;

    //判断是否在循环输入命令
    private boolean isRecycle = false;

    //循环时间间隔
    private int recycleTime;
    //循环的bytes
    private byte[] recycleBytes;

    /**
     * 侧滑布局对象，用于通过手指滑动将左侧的菜单布局进行显示或隐藏。
     */
//    private SlidingLayout slidingLayout;
//
//    private LinearLayout mLlMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_connection);
        Intent intent = getIntent();
        macStr = intent.getStringExtra("mac");
        macName = intent.getStringExtra("address");

        mContext = this;
        if (mClient == null)
            mClient = new BluetoothClient(mContext);
        init();
        showDefaultInfo();

        //slidingLayout = (SlidingLayout) findViewById(R.id.slidingLayout);
        // mLlMain = fv(R.id.ll_main);

        //slidingLayout.setScrollEvent(mRvShowOut);

        //注册连接状态的监听
        mClient.registerConnectStatusListener(macStr, mBleConnectStatusListener);
    }

    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @SuppressLint("SetTextI18n")
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {
                mConnectionState.setText(" 已连接");
                isConnection = true;
            } else if (status == STATUS_DISCONNECTED) {
                mConnectionState.setText(" 未连接");
                isConnection = false;
                //断开的时候正在连接则移除相关的任务
                if (isRecycle)
                    mHandler.removeCallbacks(runable);
            }

            hideProgressDialog();

            invalidateOptionsMenu();
        }
    };


    private void connection() {
        showProgressDialog("正在连接");

        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();

        mClient.connect(macStr, options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                //连接后获取的全部的service
                List<BleGattService> services = data.getServices();
                getAdapter(services);
            }
        });
    }

    /**
     * 显示GATT服务展示的信息
     */
    private void showGattServices() {
        if (gattServiceAdapter == null) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_gatt_services, null);
        ExpandableListView expandableListView = view.findViewById(R.id.dialog_gatt_services_list);
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
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                final BleGattService service = mGattServices.get(groupPosition);
                final BleGattCharacter characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperty();

                //点击的是可写的characteristic
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    writeUuid = characteristic.getUuid();
                    serviceUuid = service.getUUID();
                    mEtWriteCharacteristic.setText("" + characteristic.getUuid());

                    //点击的是可读的characteristic
                } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    readCharacteristic(service.getUUID(), characteristic.getUuid());
                }
                //如果点击的是通知
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {

                    mEtNotifyCharacteristic.setText("" + characteristic.getUuid());

                    notify1(service.getUUID(), characteristic.getUuid());

                    //点击的条目是INDICATE
                } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {

                }
                dialog.dismiss();
                return true;
            }
        });
    }

    //注册可通知的Characteristic的监听
    private void notify1(UUID serviceUUID, UUID characterUUID) {

        //注册通知前，先取消通知
        mClient.unnotify(macStr, serviceUUID, characterUUID, new BleUnnotifyResponse() {
            @Override
            public void onResponse(int code) {

            }
        });

        mClient.notify(macStr, serviceUUID, characterUUID, new BleNotifyResponse() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                // Log.i("hello", "onNotify: 有新消息哦" + bytesToHexString(value));
                mOutputInfo.append(HexUtil.encodeHexStr(value)).append("\n");
                mOutput.setText(" " + mOutputInfo.toString());

                if (OUT_TYPE == 0) {//文本输出
                    String s = HexUtil.encodeHexStr(value);
                    String s1 = hexStringToString(s);
                    list.add(0, s1);
                    adapter.notifyDataSetChanged();
                } else if (OUT_TYPE == 1) {//十六进制输出
                    String s = HexUtil.encodeHexStr(value);
                    list.add(0, s);
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) {
                    Toast.makeText(mContext, "通知已打开", Toast.LENGTH_SHORT).show();
                }

                //Log.i("hello", "onResponse: " + code);
            }
        });

    }

    //写入可写的Characteristic字段的监听
    private void writeCharacteristic(UUID serviceUUID, UUID characterUUID, byte[] bytes) {
        mClient.write(macStr, serviceUUID, characterUUID, bytes, new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) {
                    if (isRecycle)
                        return;
                    Toast.makeText(mContext, "发送成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "发送失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //写入可读的Characteristic字段的监听
    private void readCharacteristic(UUID serviceUUID, UUID characterUUID) {
        mClient.read(macStr, serviceUUID, characterUUID, new BleReadResponse() {
            @Override
            public void onResponse(int code, byte[] data) {
                if (code == REQUEST_SUCCESS) {
                    if (OUT_TYPE == 0) {//文本输出
                        String s = HexUtil.encodeHexStr(data);
                        String s1 = hexStringToString(s);
                        list.add(0, s1);
                        adapter.notifyDataSetChanged();
                    } else if (OUT_TYPE == 1) {//十六进制输出
                        String s = HexUtil.encodeHexStr(data);
                        list.add(0, s);
                        adapter.notifyDataSetChanged();
                    }

                } else {
                    ToastUtil.showShortToast(mContext, "读取失败");
                }
            }
        });
    }


    /**
     * 获取蓝牙服务
     *
     * @param gattServices 蓝牙服务集合
     */
    private GattServiceAdapter getAdapter(List<BleGattService> gattServices) {
        if (gattServices == null || gattServices.size() == 0) {
            return null;
        }
        String uuid;
        final String unknownServiceString = getResources().getString(R.string.unknown_service);
        final String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, Object>>> gattCharacteristicData
                = new ArrayList<>();
        //获取可操作的蓝牙服务
        for (final BleGattService gattService : gattServices) {
            final HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUUID().toString();
            currentServiceData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, Object>> gattCharacteristicGroupData =
                    new ArrayList<>();
            List<BleGattCharacter> gattCharacteristics =
                    gattService.getCharacters();
            ArrayList<BleGattCharacter> charas =
                    new ArrayList<>();

            // Loops through available Characteristics.
            for (final BleGattCharacter gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);

                final HashMap<String, Object> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                //currentCharaData.put("id",)
                int properties = gattCharacteristic.getProperty();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                currentCharaData.put(PROPERTY, properties);
                currentCharaData.put("characteristic", gattCharacteristic);

                gattCharacteristicGroupData.add(currentCharaData);
            }

            mGattServices.add(gattService);
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        gattServiceAdapter = new GattServiceAdapter(gattServiceData, gattCharacteristicData, this);

        return gattServiceAdapter;
    }

    //初始化控件
    private void init() {
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mGattUUID = (TextView) findViewById(R.id.uuid);
        mGattUUIDDesc = (TextView) findViewById(R.id.description);
        mDataAsString = (TextView) findViewById(R.id.data_as_string);
        mDataAsArray = (TextView) findViewById(R.id.data_as_array);
        //输入框
        mInput = (TextView) findViewById(R.id.input);
        mOutput = (EditText) findViewById(R.id.output);
        mEtWriteCharacteristic = (TextView) findViewById(R.id.show_write_characteristic);
        mEtWriteCharacteristic.setOnClickListener(this);
        mEtNotifyCharacteristic = (TextView) findViewById(R.id.show_notify_characteristic);
        mEtNotifyCharacteristic.setOnClickListener(this);

        mEtReadCharactertistic = fv(R.id.show_read_characteristic);
        mEtReadCharactertistic.setOnClickListener(this);


        mBtnOutPutTxt = (Button) findViewById(R.id.output_txt);
        mBtnOutPutTxt.setOnClickListener(this);
        mBtnClean = (Button) findViewById(R.id.btn_clean);
        mBtnClean.setOnClickListener(this);


        mBtnRecycleSend = fv(R.id.recycle_send);
        mBtnRecycleSend.setOnClickListener(this);

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
                        String s = list.get(i).trim();
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
                        if (list.get(i) == null) {
                            ToastUtil.showShortToast(mContext, "乱码不可转换");
                            return;
                        }

                        String s = list.get(i).trim();
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

        //展示输出的recycleView
        mRvShowOut = (RecyclerView) findViewById(R.id.rv_show_out);
        GridLayoutManager manager = new GridLayoutManager(this, 1);
        mRvShowOut.setLayoutManager(manager);
        adapter = new OutPutAdapter(list, this);
        mRvShowOut.setAdapter(adapter);

        //获取上一个activity传递过来的数据
        macStr = getIntent().getStringExtra("mac");
        macName = getIntent().getStringExtra("name");
        ((TextView) findViewById(R.id.device_address)).setText(macStr);
        ((TextView) findViewById(R.id.device_name)).setText(macName);
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
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecycle) {//正在循环
                    //终止循环
                    mHandler.removeCallbacks(runable);
                    isRecycle = false;
                    send.setText("发送");
                } else {
                    String readCharacter = mEtWriteCharacteristic.getText().toString();
                    String notifyCharacter = mEtNotifyCharacteristic.getText().toString();
                    if ("点击选择可读服务".equals(readCharacter) || "点击选择可通知服务".equals(notifyCharacter)) {
                        ToastUtil.showShortToast(mContext, "请先选择character");
                        return;
                    }
                    String userInputStr = mInput.getText().toString();
                    //16进制的指令
                    String command = "";
                    if ("点击输入命令".equals(userInputStr)) {
                        ToastUtil.showToast(mContext, "请输入指令");
                        return;
                    }
                    //如果不是16进制的，则转化为16进制的输入
                    if (!isHexData(mInput.getText().toString())) {
                        //ToastUtil.showToast(DeviceControlActivity.this, "Please input hex data command!");
                        String s = str2HexStr(mInput.getText().toString().toUpperCase());
                        command = s;
                    } else {
                        command = userInputStr.toUpperCase();
                    }

                    byte[] bytes = hexStringToByte(command);

                    writeCharacteristic(serviceUuid, writeUuid, bytes);
                    KeyCodeUtils.closeKeyCode(mContext, send);
                }
            }

        });

        //点击命令的事件
        mInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecycle) {
                    ToastUtil.showShortToast(mContext, "关闭循环后再试");
                    return;
                }
                String readCharacter = mEtWriteCharacteristic.getText().toString();
                String notifyCharacter = mEtNotifyCharacteristic.getText().toString();
                if ("点击选择可读服务".equals(readCharacter) || "点击选择可通知服务".equals(notifyCharacter)) {
                    ToastUtil.showShortToast(mContext, "请先选择character");
                    return;
                }

                String s = mInput.getText().toString();
                if ("点击输入命令".equals(s))
                    showDialog("");
                else if (!TextUtils.isEmpty(s))
                    showDialog(s);
            }
        });

    }

    /**
     * 展示输入的dialog
     *
     * @param s 文本中原本存在的命令
     */
    private void showDialog(String s) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext, R.style.Theme_AppCompat_Light_Dialog_Alert);
        final android.support.v7.app.AlertDialog dialog = builder.create();
        View view = View.inflate(mContext, R.layout.dialog_input_command, null);
        dialog.setView(view, 0, 0, 0, 0);
        final EditText editText = view.findViewById(R.id.et_user_input);
        final ImageView ivClean = view.findViewById(R.id.iv_clean);
        Button btnChange = view.findViewById(R.id.btn_change_command);
        //命令格式转换
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String command;

                String trim = editText.getText().toString().trim();
                if (TextUtils.isEmpty(trim)) {
                    ToastUtil.showShortToast(mContext, "命令不能为空");
                } else {
                    if (isHexData(trim))
                        command = hexStr2Str(trim);
                    else
                        command = str2HexStr(trim);

                    editText.setText(command);
                    editText.setSelection(command.length());
                }
            }
        });

        ivClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

        if (!TextUtils.isEmpty(s)) {
            editText.setText(s);
            editText.setSelection(s.length());
        }
        //对输入命令框进行监听
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    ivClean.setVisibility(View.VISIBLE);
                else
                    ivClean.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Spinner spinner = view.findViewById(R.id.spinner);
        int anInt = PreferencesUtils.getInt(mContext, Constant.INPUT_FLAG, 0);
        if (anInt == 0)
            spinner.setSelection(0);
        else
            spinner.setSelection(1);

        Button btnSave = view.findViewById(R.id.btn_save);
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

        //发送命令
        final Button btnOk = view.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInputStr = editText.getText().toString();
                //16进制的指令
                String command;
                if (TextUtils.isEmpty(userInputStr)) {
                    ToastUtil.showToast(mContext, "请输入指令");
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
                    String str2 = str2HexStr(editText.getText().toString().toUpperCase());
                    command = str2;
                    //command = command.toUpperCase();
                    //Log.i("hello", "onClick: "+command);
                } else {
                    command = userInputStr.toUpperCase();
                }

                byte[] bytes = hexStringToByte(command);
                writeCharacteristic(serviceUuid, writeUuid, bytes);
                dialog.dismiss();
                KeyCodeUtils.closeKeyCode(mContext, btnOk);
                //展示用户输入的命令
                mInput.setText(userInputStr);
            }
        });

        final EditText etTime = view.findViewById(R.id.et_number);

        //循环发送
        final Button btnCancel = view.findViewById(R.id.btn_recycle_send);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = etTime.getText().toString();
                if (TextUtils.isEmpty(time)) {
                    ToastUtil.showShortToast(mContext, "先填写循环间隔时间");
                    return;
                }
                recycleTime = Integer.parseInt(time);

                String userInputStr = editText.getText().toString();
                //16进制的指令
                String command;
                if (TextUtils.isEmpty(userInputStr)) {
                    ToastUtil.showToast(mContext, "请输入指令");
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
                    String str = str2HexStr(editText.getText().toString().toUpperCase());
                    command = str;
                    //command = command.toUpperCase();
                } else {
                    command = userInputStr.toUpperCase();
                }
                recycleBytes = hexStringToByte(command);
                // writeCharacteristic(serviceUuid, writeUuid, bytes);
                KeyCodeUtils.closeKeyCode(mContext, btnCancel);
                //展示用户输入的命令
                mInput.setText(userInputStr);
                //立即执行
                mHandler.post(runable);

                isRecycle = true;
                send.setText("终止循环");
                dialog.dismiss();
            }
        });

        ListView listView = view.findViewById(R.id.lv);
        //从数据库中获取保存的命令数据
        queryData = RecordDatabaseUtils.queryData(mContext);
        if (queryData.size() > 0) {
            commandAdapter = new ShowCommandAdapter(queryData);
            listView.setAdapter(commandAdapter);
            //条目点击事件
            commandAdapter.setOnItemClickLitener(new OnCommandClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    editText.setText(queryData.get(position));
                    editText.setSelection(queryData.get(position).length());
                }
            });
        }

        dialog.show();
    }

    //用于
    Handler mHandler = new Handler();

    //要执行的方法
    Runnable runable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(this, recycleTime);//每隔3s执行

            writeCharacteristic(serviceUuid, writeUuid, recycleBytes);
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        int connectStatus = mClient.getConnectStatus(macStr);

        if (connectStatus != Constants.STATUS_DEVICE_CONNECTED)
            connection();

    }

    @Override
    protected void onResume() {
        invalidateOptionsMenu();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.connect, menu);
        //showDefaultInfo();
        int status = mClient.getConnectStatus(macStr);
        if (status == Constants.STATUS_DEVICE_CONNECTED) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            mConnectionState.setText("已连接");
            mConnectionState.setTextColor(Color.GREEN);
        } else if (status == Constants.STATUS_DEVICE_DISCONNECTED) {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            mConnectionState.setText("未连接");
            mConnectionState.setTextColor(Color.RED);
            //clearUI();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect://连接设备
                connection();
                invalidateOptionsMenu();

                break;
            case R.id.menu_disconnect://断开设备
                mClient.disconnect(macStr);
                invalidateOptionsMenu();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        BusManager.getBus().unregister(this);
        //断开连接
        mClient.disconnect(macStr);
        mClient.unregisterConnectStatusListener(macStr, mBleConnectStatusListener);

        super.onDestroy();
    }

    //展示可读默认的信息
    private void showDefaultInfo() {
        mGattUUID.setText(R.string.no_data);
        mGattUUIDDesc.setText(R.string.no_data);
        mDataAsArray.setText(R.string.no_data);
        mDataAsString.setText(R.string.no_data);
        mInput.setText("点击输入命令");
        mOutput.setText("");

        mEtWriteCharacteristic.setText("点击选择可读服务");
        mEtNotifyCharacteristic.setText("点击选择可通知服务");
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

        list.clear();
        adapter.notifyDataSetChanged();
    }

    private void writeBytes(UUID serviceUUID, UUID characterUUID, String userInput) {
        String userCommand;
        if (TextUtils.isEmpty(userInput))
            Toast.makeText(mContext, "命令不能为空", Toast.LENGTH_SHORT).show();

        else {
            //如果不是16进制的，则转化为16进制的输入
            if (!isHexData(userInput)) {
                String s = str2HexStr(userInput);
                userCommand = s;
            } else {
                userCommand = userInput;
            }

            byte[] bytes = hexStringToByte(userCommand);
            this.writeCharacteristic(serviceUUID, characterUUID, bytes);
        }
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
        progressDialog = new Dialog(mContext, R.style.progress_dialog);
        progressDialog.setContentView(R.layout.progress);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.gray_bg);
        TextView msg = progressDialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText(s);
        progressDialog.show();

        //当dialog被dismiss的时候停止连接
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //如果正在连接则取消连接
                //mClient.disconnection(macStr);
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


            case R.id.show_read_characteristic:

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
//            case R.id.btn_show_command:
//                List<String> list = new ArrayList<>();
//                List<String> queryData = RecordDatabaseUtils.queryData(DeviceConnectionActivity.this);
//                if (queryData.size() > 0 && queryData.size() < 9)
//                    initPopupWindow(mBtnShowCommand, queryData);
//                else if (queryData.size() > 8) {
//                    for (int i = queryData.size() - 9; i < queryData.size(); i++) {
//                        list.add(queryData.get(i));
//                    }
//                    initPopupWindow(mBtnShowCommand, list);
//                } else
//                    ToastUtil.showShortToast(mContext, "当前设备没有保存命令");
//                break;

            //循环发送命令
            case R.id.recycle_send:


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

        ListView lvShow = view.findViewById(R.id.lv_show);
//        commandAdapter = new ShowCommandAdapter(queryData);
//        lvShow.setAdapter(commandAdapter);

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

                popupWindow.dismiss();

            }
        });
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
                ("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");// HH:mm:ss
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
                // openAssignFolder(filePath);
                dialog.dismiss();
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

    //绑定控件
    private <T extends View> T fv(int resId) {
        return (T) this.findViewById(resId);
    }


}
