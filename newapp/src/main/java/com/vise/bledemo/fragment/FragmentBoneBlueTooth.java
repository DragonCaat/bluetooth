package com.vise.bledemo.fragment;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
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

import com.vise.bledemo.R;
import com.vise.bledemo.adapter.SearchBluetoothAdapter;
import com.vise.bledemo.bean.BluetoothEntity;
import com.vise.bledemo.common.OnRecycleViewItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;



/**
 * 已经绑定的fragment
 */
public class FragmentBoneBlueTooth extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private View view;

    private Context mContext;
    // 获取到蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    // 用来保存搜索到的设备信息
    private List<BluetoothEntity> bluetoothDevices = new ArrayList<>();
    //已经配对过的蓝牙设备
    private Set<BluetoothDevice> bondedDevices;

    //蓝牙搜索的适配器
    private SearchBluetoothAdapter adapter;

    RecyclerView mRvBonedBluetooth;

    SwipeRefreshLayout mSwipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_boned, null);
        init();
        mContext = getActivity();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // null:表示不支持蓝牙

        getSearchedBlueTooth();
        GridLayoutManager manager = new GridLayoutManager(mContext, 1);
        mRvBonedBluetooth.setLayoutManager(manager);

        mSwipeRefresh.setOnRefreshListener(this);
        return view;
    }

    private void init() {
        mRvBonedBluetooth = view.findViewById(R.id.rv_bone_bluetooth);
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //unbinder.unbind();
    }

    //获取已经配对过的蓝牙设备
    private void getSearchedBlueTooth() {
        mSwipeRefresh.setRefreshing(true);
        //手机有蓝牙设备并且蓝牙是打开的
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
            bondedDevices = mBluetoothAdapter.getBondedDevices(); // 获取已经配对的蓝牙设备
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice device : bondedDevices) {
                //tvDevices.append(device.getName() + ":" + device.getAddress() + "\n");
                BluetoothEntity bluetoothEntity = new BluetoothEntity();
                bluetoothEntity.setName(device.getName());
                bluetoothEntity.setAddress(device.getAddress());
                bluetoothEntity.setConnection(1);
                bluetoothEntity.setDevice(device);

                int type = device.getBluetoothClass().getDeviceClass();//蓝牙设备的类型

                bluetoothEntity.setType(type);

                bluetoothDevices.add(bluetoothEntity);
            }
            mSwipeRefresh.setRefreshing(false);

            adapter = new SearchBluetoothAdapter(mContext, bluetoothDevices, new OnRecycleViewItemClickListener() {
                @Override
                public void onItemClickListener(View v, int position) {
                    //跳转，并把数据点击的device传到第二个界面
                    BluetoothDevice device = bluetoothDevices.get(position).getDevice();


                }
            });

            mRvBonedBluetooth.setAdapter(adapter);
        }
    }


    @Override
    public void onRefresh() {
        if (bluetoothDevices!=null)
            bluetoothDevices.clear();

        getSearchedBlueTooth();
    }
}

