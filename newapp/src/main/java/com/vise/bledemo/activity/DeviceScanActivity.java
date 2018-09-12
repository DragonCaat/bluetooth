package com.vise.bledemo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.RegularFilterScanCallback;
import com.vise.baseble.callback.scan.ScanCallback;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;
import com.vise.bledemo.R;
import com.vise.bledemo.adapter.DeviceAdapter;
import com.vise.bledemo.common.KeyCodeUtils;
import com.vise.bledemo.common.ToastUtil;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 设备扫描展示界面
 */
public class DeviceScanActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 100;

    private ListView deviceLv;
    private TextView scanCountTv;

    //设备扫描结果展示适配器
    private DeviceAdapter adapter;

    private BluetoothLeDeviceStore bluetoothLeDeviceStore = new BluetoothLeDeviceStore();

    private Context context;

    private int scanKind = 0;//0:全部扫描  1：手环 2：手表

    private List<BluetoothLeDevice> deviceList = new ArrayList<>();
    /**
     * 扫描回调
     */
    private ScanCallback periodScanCallback = new ScanCallback(new IScanCallback() {
        @Override
        public void onDeviceFound(final BluetoothLeDevice bluetoothLeDevice) {
            // ViseLog.i("hello" + bluetoothLeDevice);
            bluetoothLeDeviceStore.addDevice(bluetoothLeDevice);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter != null && bluetoothLeDeviceStore != null) {
                        if (deviceList != null)

                            deviceList.clear();

                        deviceList.addAll(bluetoothLeDeviceStore.getDeviceList());
                        adapter.setListAll(deviceList);
                        //排序
                        Collections.sort(deviceList, new SortByRssi());
                        adapter.clear();
                        adapter.setListAll(deviceList);

                        updateItemCount(adapter.getCount());
                    }
                }
            });
        }

        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
            // ViseLog.i("hello" + bluetoothLeDeviceStore);
        }

        @Override
        public void onScanTimeout() {
            ViseLog.i("scan timeout");
            ToastUtil.showShortToast(DeviceScanActivity.this, "扫描ch");
        }

    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);
        init();
        context = this;
    }


    private void init() {
        deviceLv = (ListView) findViewById(android.R.id.list);
        scanCountTv = (TextView) findViewById(R.id.scan_device_count);

        adapter = new DeviceAdapter(this, scanKind);
        deviceLv.setAdapter(adapter);

        deviceLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //点击某个扫描到的设备进入设备详细信息界面
                BluetoothLeDevice device = (BluetoothLeDevice) adapter.getItem(position);
                if (device == null) return;
//                Intent intent = new Intent(DeviceScanActivity.this, DeviceDetailActivity.class);
//                intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE, device);
//                startActivity(intent);

//                Intent intent = new Intent(DeviceScanActivity.this, DeviceControlActivity.class);
//                intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE, device);
//                startActivity(intent);

                Intent intent = new Intent(DeviceScanActivity.this, DeviceConnectionActivity.class);
                intent.putExtra("name", device.getName());
                intent.putExtra("mac", device.getAddress());
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startScan();
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
        invalidateOptionsMenu();
        bluetoothLeDeviceStore.clear();
    }

    @Override
    protected void onDestroy() {
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
        getMenuInflater().inflate(R.menu.scan, menu);
        if (periodScanCallback != null && !periodScanCallback.isScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_progress_indeterminate);
        }
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
            case R.id.menu_scan://开始扫描
                startScan();
                //showScanSelect();
                break;
            case R.id.menu_stop://停止扫描
                stopScan();
                break;
        }
        return true;
    }

    /**
     * 开始扫描所有的设备
     *
     * @deprecated 该方式扫描是扫描所有的设备，永久扫描
     */
    private void startScan() {
        updateItemCount(0);
        if (adapter != null) {
            adapter.setListAll(new ArrayList<BluetoothLeDevice>());
        }
        ViseBle.getInstance().startScan(periodScanCallback);
        invalidateOptionsMenu();
    }

    /**
     * 扫描一定范围内的设备
     *
     * @param rssi 信号强度
     */
    private void startScan(int rssi) {
        updateItemCount(0);
        if (adapter != null) {
            adapter.setListAll(new ArrayList<BluetoothLeDevice>());
        }
        ViseBle.getInstance().startScan(new RegularFilterScanCallback(new IScanCallback() {
            @Override
            public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
                bluetoothLeDeviceStore.addDevice(bluetoothLeDevice);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter != null && bluetoothLeDeviceStore != null) {
                            adapter.setListAll(bluetoothLeDeviceStore.getDeviceList());
                            updateItemCount(adapter.getCount());
                        }
                    }
                });
            }

            @Override
            public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {

            }

            @Override
            public void onScanTimeout() {

            }
        }).setDeviceRssi(rssi));

        invalidateOptionsMenu();
    }

    /**
     * 停止扫描
     */
    private void stopScan() {
        if (periodScanCallback != null)
            ViseBle.getInstance().stopScan(periodScanCallback);

        invalidateOptionsMenu();

//        Collections.sort(deviceList, new SortByRssi());
//        adapter.clear();
//        adapter.setListAll(deviceList);
    }

    //排序
    class SortByRssi implements Comparator {
        public int compare(Object o1, Object o2) {
            BluetoothLeDevice s1 = (BluetoothLeDevice) o1;
            BluetoothLeDevice s2 = (BluetoothLeDevice) o2;
            if (s1.getRssi() < s2.getRssi())
                return 1;
            if (s1.getRssi() == s2.getRssi())
                return 0;
            return -1;
        }
    }

    /**
     * 更新扫描到的设备个数
     *
     * @param count
     */
    private void updateItemCount(final int count) {
        scanCountTv.setText(getString(R.string.formatter_item_count, String.valueOf(count)));
    }

    /**
     * 弹出搜索选择框
     */
    private void showScanSelect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
        final AlertDialog dialog = builder.create();

        View view = View.inflate(context, R.layout.dailog_user_scan, null);
        // dialog.setView(view);// 将自定义的布局文件设置给dialog
        dialog.setView(view, 0, 0, 0, 0);// 设置边距为0,保证在2.x的版本上运行没问题

        final EditText etUserInPut = (EditText) view.findViewById(R.id.et_user_input);

        //用户输入数字后调用
        final Button btnOK = (Button) view.findViewById(R.id.btn_ok);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = etUserInPut.getText().toString();
                if (TextUtils.isEmpty(s))
                    Toast.makeText(context, "输入不能为空", Toast.LENGTH_SHORT).show();
                else {
                    KeyCodeUtils.closeKeyCode(context, etUserInPut);
                    dialog.dismiss();

                    int power = Integer.parseInt(s);
                    //double power = (bluetoothLeDevice.getRssi() - 59) / 25.0;
                    //将距离转化为蓝牙信号
                    int rssi = (int) (power * 25 + 59);

                    startScan(rssi);
                }

            }
        });

        dialog.show();

        //搜索范围内的设备
        Button btnSearchByRssi = (Button) view.findViewById(R.id.btn_search_by_rssi);
        btnSearchByRssi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etUserInPut.setVisibility(View.VISIBLE);
                btnOK.setVisibility(View.VISIBLE);
            }
        });

        //搜索全部设备
        Button btnSearchAll = (Button) view.findViewById(R.id.btn_search_all);
        btnSearchAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
                dialog.dismiss();
            }
        });
    }

}
