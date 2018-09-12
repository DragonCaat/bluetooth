/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vise.bledemo.common;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        //服务编码
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");

        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "GAP");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "GATT");
        attributes.put("00001802-0000-1000-8000-00805f9b34fb", "IMMEDIATE ALERT");
        attributes.put("00001803-0000-1000-8000-00805f9b34fb", "LINK LOSS");
        attributes.put("00001804-0000-1000-8000-00805f9b34fb", "TX POWER");

        attributes.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");

        // Sample Characteristics.Peripheral Preferred Connection Parameters
        //特征编码
        attributes.put(HEART_RATE_MEASUREMENT, "心率测量");//Heart Rate Measurement
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");//制造商名称字符串
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device name");//设备名称

        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");//外观
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preferred Connection Parameters");//周围的优先连接参数
        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");//Serial Number String更改服务
        attributes.put("00002a06-0000-1000-8000-00805f9b34fb", "Alert Level");//警戒级别
        attributes.put("00002a07-0000-1000-8000-00805f9b34fb", "Tx Power Level");//功率电平
        attributes.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");//电池电量

        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");//模型编号
        attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");//设备序列号
        attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");//固件版本号
        attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");//硬件版本字号

    }

    /**
     * 根据序列号查询对应的名称
     * @param uuid        设备唯一的识别码
     * @param defaultName 默认名称
     */
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
