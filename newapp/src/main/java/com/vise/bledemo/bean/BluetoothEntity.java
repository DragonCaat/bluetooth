package com.vise.bledemo.bean;


import android.bluetooth.BluetoothDevice;

/**
 * 蓝牙信息的实体
 *
 * @author Darcy
 */
public class BluetoothEntity {

    private String name;
    private String address;

    private int connection;//1:已连接 0：未连接

    private BluetoothDevice device;

    private String distance;

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private short rssi;

    public short getRssi() {
        return rssi;
    }

    public void setRssi(short rssi) {
        this.rssi = rssi;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getConnection() {
        return connection;
    }

    public void setConnection(int connection) {
        this.connection = connection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
