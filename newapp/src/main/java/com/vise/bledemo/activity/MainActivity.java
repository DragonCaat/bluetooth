package com.vise.bledemo.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothClient;
import com.vise.baseble.ViseBle;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.BleUtil;
import com.vise.bledemo.R;
import com.vise.bledemo.adapter.DeviceMainAdapter;
import com.vise.bledemo.adapter.MainAdapter;
import com.vise.bledemo.bean.MainBean;
import com.vise.bledemo.common.BluetoothDeviceManager;
import com.vise.bledemo.common.ToastUtil;
import com.vise.bledemo.event.ConnectEvent;
import com.vise.bledemo.event.NotifyDataEvent;
import com.vise.bledemo.receiver.MyReceiver;
import com.vise.bledemo.utils.TimeUtils;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.app.Activity.RESULT_CANCELED;

/**
 * @Description: 主页，展示已连接设备列表
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/10/20 17:35
 */
public class MainActivity extends AppCompatActivity {

    private TextView supportTv;
    private TextView statusTv;
    private ListView deviceLv;
    private TextView emptyTv;
    private TextView countTv;

    private TextView tv;

    private DeviceMainAdapter adapter;

    private Context mContext;

    //适配器
    private MainAdapter mainAdapter;

    //底部弹出框
    private Dialog dialog;

    private List<MainBean> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViseLog.getLogConfig().configAllowLog(true);//配置日志信息
        ViseLog.plant(new LogcatTree());//添加Logcat打印信息

        BluetoothDeviceManager.getInstance().init(this);
        BusManager.getBus().register(this);

        init();
        mContext = this;


        //动态注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("myAction");
        this.registerReceiver(myReceiver, filter);


        FloatingActionButton viewById = (FloatingActionButton) findViewById(R.id.fb1);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setAction("myAction");
//                intent.putExtra("mac", "Hi!I am broadcastData!");
//                intent.putExtra("name", "Hi!I am broadcastData!");
//                sendBroadcast(intent);
            }
        });

    }

    private void init() {
        supportTv = (TextView) findViewById(R.id.main_ble_support);
        statusTv = (TextView) findViewById(R.id.main_ble_status);
        deviceLv = (ListView) findViewById(android.R.id.list);
        emptyTv = (TextView) findViewById(android.R.id.empty);
        countTv = (TextView) findViewById(R.id.connected_device_count);

        tv = (TextView) findViewById(R.id.tv);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DeviceScanActivity.class);
                startActivity(intent);
            }
        });

//        adapter = new DeviceMainAdapter(this);
//        deviceLv.setAdapter(adapter);

        mainAdapter = new MainAdapter(list, this);
        deviceLv.setAdapter(mainAdapter);


        deviceLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                BluetoothLeDevice device = (BluetoothLeDevice) adapter.getItem(position);
//                if (device == null) return;
                Intent intent = new Intent(MainActivity.this, DeviceConnectionActivity.class);

                Log.i("hello", "onItemClick: "+list.get(position).getMacStr()+"\n"+list.get(position).getName());

                intent.putExtra("name", list.get(position).getMacStr());
                intent.putExtra("mac", list.get(position).getName());
                intent.putExtra("flag",1);
                startActivity(intent);


            }
        });
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String flag = intent.getStringExtra("flag");

            if ("1".equals(flag)) {
                String macStr = intent.getStringExtra("mac");
                String name = intent.getStringExtra("name");
               // Log.i("hello", "onReceive: " + name);
                MainBean mainBean = new MainBean(name, macStr);

                list.add(mainBean);
                mainAdapter.notifyDataSetChanged();
            }else if ("0".equals(flag)){
                if (list.size()>0){
                    for (int i=0;i<list.size();i++){
                        if (list.get(i).getName().equals(intent.getStringExtra("mac")))
                            list.remove(i);
                    }
                    mainAdapter.notifyDataSetChanged();
                }
            }
            Log.i("hello", "onReceive: "+flag);

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mainAdapter.notifyDataSetChanged();
    }

    /**
     * 展示已经连接的涉笔
     *
     * @param event 连接的事件
     */
    @Subscribe
    public void showConnectedDevice(ConnectEvent event) {
        if (event != null) {
            updateConnectedDevice();
            if (event.isDisconnected()) {
                ToastUtil.showToast(MainActivity.this, "Disconnect!");
            }

            // Log.i("hello", "showConnectedDevice: ");
        }
    }


    @Subscribe
    public void showDeviceNotifyData(NotifyDataEvent event) {
        if (event != null && adapter != null) {
            adapter.setNotifyData(event.getBluetoothLeDevice(), event.getData());

            // Log.i("hello", "showConnectedDevice: 我也是");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //检查蓝牙权限
        checkBluetoothPermission();
    }

    @Override
    protected void onDestroy() {
        ViseBle.getInstance().clear();
        BusManager.getBus().unregister(this);

        this.unregisterReceiver(myReceiver);

        super.onDestroy();
    }

    /**
     * 菜单栏的显示
     *
     * @param menu 菜单
     * @return 返回是否拦截操作
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }

    /**
     * 点击菜单栏的处理
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about://关于
                displayAboutDialog();
                break;

            case R.id.menu_history://历史连接
                // showDialog();
                //BluetoothLeDevice leDevices = DatabaseUtils.getObjectArray(mContext);

                //Log.i("hello", "showDialog: "+leDevices);
                break;
        }
        return true;
    }

    /**
     * 打开或关闭蓝牙后的回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                statusTv.setText(getString(R.string.on));
                enableBluetooth();
            }
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 检查蓝牙权限
     */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionManager.instance().with(this).request(new OnPermissionCallback() {
                    @Override
                    public void onRequestAllow(String permissionName) {
                        enableBluetooth();
                    }

                    @Override
                    public void onRequestRefuse(String permissionName) {
                        finish();
                    }

                    @Override
                    public void onRequestNoAsk(String permissionName) {
                        finish();
                    }
                }, Manifest.permission.ACCESS_COARSE_LOCATION);
            } else {
                enableBluetooth();
            }
        } else {
            enableBluetooth();
        }
    }

    private void enableBluetooth() {
        if (!BleUtil.isBleEnable(this)) {
            BleUtil.enableBluetooth(this, 1);
        } else {
            boolean isSupport = BleUtil.isSupportBle(this);
            boolean isOpenBle = BleUtil.isBleEnable(this);
            if (isSupport) {
                supportTv.setText(getString(R.string.supported));
            } else {
                supportTv.setText(getString(R.string.not_supported));
            }
            if (isOpenBle) {
                statusTv.setText(getString(R.string.on));
            } else {
                statusTv.setText(getString(R.string.off));
            }
            invalidateOptionsMenu();
            updateConnectedDevice();
        }
    }

    /**
     * 更新已经连接到的设备
     */
    private void updateConnectedDevice() {
        if (adapter != null && ViseBle.getInstance().getDeviceMirrorPool() != null) {
            List<BluetoothLeDevice> bluetoothLeDeviceList = ViseBle.getInstance().getDeviceMirrorPool().getDeviceList();
            if (bluetoothLeDeviceList != null && bluetoothLeDeviceList.size() > 0) {
               // deviceLv.setVisibility(View.VISIBLE);
            } else {
                //deviceLv.setVisibility(View.GONE);
            }
            adapter.setListAll(bluetoothLeDeviceList);
            updateItemCount(adapter.getCount());
        } else {
           // deviceLv.setVisibility(View.GONE);
        }
    }

    /**
     * 更新已经连接的设备个数
     *
     * @param count
     */
    private void updateItemCount(final int count) {
        countTv.setText(getString(R.string.formatter_item_count, String.valueOf(count)));
    }

    /**
     * 显示项目信息
     * 展示关于我们的dialog
     */
    private void displayAboutDialog() {
        final int paddingSizeDp = 5;
        final float scale = getResources().getDisplayMetrics().density;
        final int dpAsPixels = (int) (paddingSizeDp * scale + 0.5f);

        final TextView textView = new TextView(this);
        final SpannableString text = new SpannableString(getString(R.string.about_dialog_text));

        textView.setText(text);
        textView.setAutoLinkMask(RESULT_OK);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);

        Linkify.addLinks(text, Linkify.ALL);
        new AlertDialog.Builder(this).setTitle(R.string.menu_about).setCancelable(false).setPositiveButton(android.R
                .string.ok, null)
                .setView(textView).show();
    }

    /**
     * 展示底部弹出的dialog
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void showDialog() {
        dialog = new Dialog(this, R.style.MyDialog);
        //填充对话框的布局
        View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);

        //将布局设置给Dialog
        dialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow == null) {
            return;
        }
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.CENTER);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //lp.y = 30;//设置Dialog距离底部的距离
        //将属性设置给窗体
        dialogWindow.setAttributes(lp);
        dialog.show();//显示对话框


    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

}
