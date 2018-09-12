package com.vise.bledemo.bean;

import com.vise.baseble.model.BluetoothLeDevice;

import java.io.Serializable;

public class BluetoothLeDeviceEntity implements Serializable{

    private BluetoothLeDevice bluetoothLeDevice;

    public BluetoothLeDeviceEntity(BluetoothLeDevice bluetoothLeDevice){
        this.bluetoothLeDevice = bluetoothLeDevice;
       // this.age  = age;
    }
    public BluetoothLeDevice getBluetoothLeDevice() {
        return bluetoothLeDevice;
    }

    public void setBluetoothLeDevice(BluetoothLeDevice bluetoothLeDevice) {
        this.bluetoothLeDevice = bluetoothLeDevice;
    }
}
