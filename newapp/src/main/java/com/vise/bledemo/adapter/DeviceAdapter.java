package com.vise.bledemo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.HexUtil;
import com.vise.bledemo.R;
import com.vise.bledemo.activity.DeviceConnectionActivity;
import com.vise.bledemo.activity.DeviceControlActivity;
import com.vise.bledemo.activity.DeviceDetailActivity;
import com.vise.bledemo.activity.DeviceScanActivity;
import com.vise.xsnow.ui.adapter.helper.HelperAdapter;
import com.vise.xsnow.ui.adapter.helper.HelperViewHolder;

import java.util.Formatter;

import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET;
import static android.bluetooth.BluetoothClass.Device.COMPUTER_LAPTOP;
import static android.bluetooth.BluetoothClass.Device.PHONE_SMART;
import static android.bluetooth.BluetoothClass.Device.WEARABLE_WRIST_WATCH;
import static java.lang.Math.pow;

public class DeviceAdapter extends HelperAdapter<BluetoothLeDevice> {

    private int scanKind; //0:全部 1：手环 2：手表
    private Context context;

    public DeviceAdapter(Context context, int scanKind) {
        super(context, R.layout.item_scan_layout);
        this.scanKind = scanKind;
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void HelpConvert(HelperViewHolder viewHolder, int position, final BluetoothLeDevice bluetoothLeDevice) {
        TextView deviceNameTv = viewHolder.getView(R.id.device_name);
        TextView deviceMacTv = viewHolder.getView(R.id.device_mac);
        TextView deviceRssiTv = viewHolder.getView(R.id.device_rssi);
        TextView deviceScanRecordTv = viewHolder.getView(R.id.device_scanRecord);

        ImageView deviceImage = viewHolder.getView(R.id.device_image);
        TextView deviceDistance = viewHolder.getView(R.id.device_distance);

        TextView deviceBoned = viewHolder.getView(R.id.tv_boned);


        LinearLayout llBluetooth = viewHolder.getView(R.id.ll_show_bluetooth);
        llBluetooth.setVisibility(View.VISIBLE);
        if (bluetoothLeDevice != null && bluetoothLeDevice.getDevice() != null) {
            final String deviceName = bluetoothLeDevice.getDevice().getName();
            if (deviceName != null && !deviceName.isEmpty()) {
                deviceNameTv.setText(deviceName);
            } else {
                //llBluetooth.setVisibility(View.GONE);
                return;
            }

            deviceImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DeviceControlActivity.class);
                intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE, bluetoothLeDevice);
                context.startActivity(intent);
                }
            });


            deviceNameTv.setText(deviceName);
            deviceMacTv.setText(bluetoothLeDevice.getDevice().getAddress());
            deviceRssiTv.setText("" + bluetoothLeDevice.getRssi());

            deviceScanRecordTv.setText(mContext.getString(R.string.header_scan_record) + ":"
                    + HexUtil.encodeHexStr(bluetoothLeDevice.getScanRecord()));
            deviceImage.setBackgroundResource(R.drawable.circle_bg);

            if (bluetoothLeDevice.getDevice().getBluetoothClass().getDeviceClass() == PHONE_SMART)
                deviceImage.setImageResource(R.mipmap.phone);//智能手机
            else if (bluetoothLeDevice.getDevice().getBluetoothClass().getDeviceClass() == COMPUTER_LAPTOP)
                deviceImage.setImageResource(R.mipmap.computer);//笔记本
            else if (bluetoothLeDevice.getDevice().getBluetoothClass().getDeviceClass() == AUDIO_VIDEO_WEARABLE_HEADSET)
                deviceImage.setImageResource(R.mipmap.bluetooth_earphone);//蓝牙耳机
            else if (bluetoothLeDevice.getDevice().getBluetoothClass().getDeviceClass() == WEARABLE_WRIST_WATCH)
                deviceImage.setImageResource(R.mipmap.bluetooth_circle);//手表
            else if (bluetoothLeDevice.getDevice().getBluetoothClass().getDeviceClass() == 7936)
                deviceImage.setImageResource(R.mipmap.bluetooth_circle);//手环
            else
                deviceImage.setImageResource(R.mipmap.bluetooth_circle);

            //bluetooth_common  另一种形式的蓝牙图标

            // 将蓝牙信号强度换算为距离
            double power = (bluetoothLeDevice.getRssi() - 59) / 25.0;

            deviceDistance.setText("" + (-power));


            deviceBoned.setText(""+bluetoothLeDevice.getBluetoothDeviceBondState());


        }

    }
}
