package com.vise.bledemo.fragment;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.vise.bledemo.R;
import com.vise.bledemo.adapter.SearchBluetoothAdapter;
import com.vise.bledemo.bean.BluetoothEntity;
import com.vise.bledemo.common.OnRecycleViewItemClickListener;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Set;


import static java.lang.Math.abs;
import static java.lang.Math.pow;

/**
 * 未绑定的fragment
 */
public class FragmentUnBoneBlueTooth extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private static final int BLUETOOTH_REQUEST = 1;
    private View view;
   // private Unbinder unbinder;

    private Context mContext;
    // 获取到蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    // 用来保存搜索到的设备信息
    private List<BluetoothEntity> bluetoothDevices = new ArrayList<>();
    //已经配对过的蓝牙设备
    private Set<BluetoothDevice> bondedDevices;

    //蓝牙搜索的适配器
    private SearchBluetoothAdapter adapter;


    //展示搜索框
    private ProgressDialog progressDialog;

    RecyclerView mRvBonedBluetooth;

    SwipeRefreshLayout mSwipeRefresh;

    private Button mBtnSearch;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_unboned, null);
        init();
        mContext = getActivity();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // null:表示不支持蓝牙

        getSearchedBlueTooth();
        GridLayoutManager manager = new GridLayoutManager(mContext, 1);
        mRvBonedBluetooth.setLayoutManager(manager);

        initBroadCastReceiver();

        mSwipeRefresh.setOnRefreshListener(this);

        return view;
    }

    private void init() {
        mRvBonedBluetooth = view.findViewById(R.id.rv_bone_bluetooth);
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh);
        mBtnSearch = view.findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBlueTooth();
            }
        });

    }

    private void initBroadCastReceiver() {
        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);//每搜索到一个设备就会发送一个该广播
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//当全部搜索完后发送该广播
        intentFilter.setPriority(Integer.MAX_VALUE);//设置优先级
        getActivity().registerReceiver(receiver, intentFilter);
    }

    //搜索蓝牙
    private void searchBlueTooth() {
        boolean enabled = mBluetoothAdapter.isEnabled(); // true:处于打开状态, false:处于关闭状态
        if (enabled) {
            //如果正在搜索则先取消搜索
            if (mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();
            showProgressDialog("正在搜索");

            mBluetoothAdapter.startDiscovery();
        } else {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_REQUEST);
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //unbinder.unbind();
    }

    //获取已经配对过的蓝牙设备
    private void getSearchedBlueTooth() {
        adapter = new SearchBluetoothAdapter(mContext, bluetoothDevices, new OnRecycleViewItemClickListener() {
            @Override
            public void onItemClickListener(View v, int position) {
                //跳转，并把数据点击的device传到第二个界面
                BluetoothDevice device = bluetoothDevices.get(position).getDevice();


            }
        });

        mRvBonedBluetooth.setAdapter(adapter);
    }


    /**
     * 定义广播接收器
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //有新的蓝牙设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    BluetoothEntity bluetoothEntity = new BluetoothEntity();
                    bluetoothEntity.setName(device.getName());
                    bluetoothEntity.setAddress(device.getAddress());
                    bluetoothEntity.setConnection(0);
                    bluetoothEntity.setDevice(device);

                    short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                    int iRssi = abs(rssi);
                    // 将蓝牙信号强度换算为距离
                    double power = (iRssi - 59) / 25.0;
                    String mm = new Formatter().format("%.2f", pow(10, power)).toString();
                    bluetoothEntity.setDistance(mm);

                    bluetoothEntity.setRssi(rssi);
                    int type = device.getBluetoothClass().getDeviceClass();//蓝牙设备的类型
                    bluetoothEntity.setType(type);

                    bluetoothDevices.add(bluetoothEntity);
                    adapter.notifyDataSetChanged();
                }


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //已搜素完成
                hideProgressDialog();
                adapter.notifyDataSetChanged();
            }
        }
    };

    //展示搜索框
    private void showProgressDialog(String s) {
        if (progressDialog != null)
            progressDialog = null;
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(s);
        progressDialog.show();

        //当dialog被dismiss的时候停止搜索
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //如果正在搜索则取消搜索
                if (mBluetoothAdapter.isDiscovering())
                    mBluetoothAdapter.cancelDiscovery();
            }
        });
    }

    //隐藏搜索框
    private void hideProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onRefresh() {
        if (bluetoothDevices!=null)
            bluetoothDevices.clear();
        searchBlueTooth();

        mSwipeRefresh.setRefreshing(false);
    }
}

