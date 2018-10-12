package com.vise.bledemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author : Darcy
 * @Date ${Date}
 * @Description 自定义广播
 */
public class MyReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("hello", "onReceive: "+intent.getStringExtra("data"));
    }
}
