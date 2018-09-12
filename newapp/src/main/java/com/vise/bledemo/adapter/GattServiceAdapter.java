package com.vise.bledemo.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.connect.listener.WriteCharacterListener;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.vise.baseble.utils.HexUtil;
import com.vise.bledemo.R;
import com.vise.bledemo.activity.DeviceConnectionActivity;
import com.vise.bledemo.common.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.vise.bledemo.activity.DeviceConnectionActivity.hexStringToString;

/**
 * gatt服务的适配器
 *
 * @author darcy
 */
public class GattServiceAdapter extends BaseExpandableListAdapter {
    public static final String EXTRA_DEVICE = "extra_device";

    private ArrayList<HashMap<String, String>> gattServiceData;
    private ArrayList<ArrayList<HashMap<String, Object>>> gattCharacteristicData;
    private Context context;

    public GattServiceAdapter(ArrayList<HashMap<String, String>> gattServiceData,
                              ArrayList<ArrayList<HashMap<String, Object>>> gattCharacteristicData,
                              Context context) {
        this.gattServiceData = gattServiceData;
        this.gattCharacteristicData = gattCharacteristicData;
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return gattServiceData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<HashMap<String, Object>> hashMaps = gattCharacteristicData.get(groupPosition);

        return hashMaps.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return gattServiceData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return gattCharacteristicData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View convertView, ViewGroup viewGroup) {
        HashMap<String, String> map = gattServiceData.get(i);
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_gatt_service_item, viewGroup, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.tvServiceName = (TextView) convertView.findViewById(R.id.tv_service_name);
            groupViewHolder.tvUUid = (TextView) convertView.findViewById(R.id.tv_service_uuid);
            convertView.setTag(groupViewHolder);
        } else
            groupViewHolder = (GroupViewHolder) convertView.getTag();

        groupViewHolder.tvServiceName.setText(map.get("NAME"));
        groupViewHolder.tvUUid.setText(map.get("UUID"));
        return convertView;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean b, View convertView, ViewGroup viewGroup) {
        final ArrayList<HashMap<String, Object>> hashMaps = gattCharacteristicData.get(groupPosition);
        final HashMap<String, String> map = gattServiceData.get(groupPosition);
        final ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_gatt_character_item, viewGroup, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.tvCharacterName = (TextView) convertView.findViewById(R.id.tv_service_name);
            childViewHolder.tvUUid = (TextView) convertView.findViewById(R.id.tv_service_uuid);
            childViewHolder.tvProperties = (TextView) convertView.findViewById(R.id.tv_properties);
            childViewHolder.ivWrite = (ImageView) convertView.findViewById(R.id.iv_write);
            childViewHolder.ivReceive = (ImageView) convertView.findViewById(R.id.iv_receive);
            childViewHolder.tvValue = (TextView) convertView.findViewById(R.id.tv_value);
            childViewHolder.ivRead = convertView.findViewById(R.id.iv_read);

            childViewHolder.ivRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    readCharacteristic(UUID.fromString(gattServiceData.get(groupPosition).get("UUID")),
                            UUID.fromString((String) hashMaps.get(childPosition).get("UUID")),
                            childViewHolder.tvValue);

                }
            });

            convertView.setTag(childViewHolder);
        } else
            childViewHolder = (ChildViewHolder) convertView.getTag();

        childViewHolder.tvCharacterName.setText("" + hashMaps.get(childPosition).get("NAME"));
        childViewHolder.tvUUid.setText("" + hashMaps.get(childPosition).get("UUID"));
        childViewHolder.tvValue.setText("" + hashMaps.get(childPosition).get("value"));

        //设置characteristic的读写权限
        final int charaProp = (int) hashMaps.get(childPosition).get("PROPERTY");
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            childViewHolder.tvProperties.setText("READ");
            childViewHolder.ivWrite.setVisibility(View.GONE);
            childViewHolder.ivReceive.setVisibility(View.GONE);
            childViewHolder.ivRead.setVisibility(View.VISIBLE);
        }

        //既可读又可写
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 && (charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            childViewHolder.tvProperties.setText("WRITE_NO_RESPONSE   NOTIFY");
            childViewHolder.ivWrite.setVisibility(View.VISIBLE);
            childViewHolder.ivReceive.setVisibility(View.GONE);
            childViewHolder.ivRead.setVisibility(View.GONE);
        }

        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            childViewHolder.tvProperties.setText("WRITE");
            childViewHolder.ivWrite.setVisibility(View.VISIBLE);
            childViewHolder.ivReceive.setVisibility(View.GONE);
            childViewHolder.ivRead.setVisibility(View.GONE);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
            childViewHolder.tvProperties.setText("WRITE_NO_RESPONSE");
            childViewHolder.ivWrite.setVisibility(View.VISIBLE);
            childViewHolder.ivReceive.setVisibility(View.GONE);
            childViewHolder.ivRead.setVisibility(View.GONE);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            childViewHolder.tvProperties.setText("NOTIFY");
            childViewHolder.ivWrite.setVisibility(View.GONE);
            childViewHolder.ivReceive.setVisibility(View.VISIBLE);
            childViewHolder.ivRead.setVisibility(View.GONE);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            childViewHolder.tvProperties.setText("INDICATE");
            childViewHolder.ivWrite.setVisibility(View.GONE);
            childViewHolder.ivReceive.setVisibility(View.GONE);
            childViewHolder.ivRead.setVisibility(View.GONE);
        }


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }


    static class GroupViewHolder {
        TextView tvServiceName;
        TextView tvUUid;
    }

    static class ChildViewHolder {
        TextView tvCharacterName;
        TextView tvUUid;
        TextView tvProperties;
        ImageView ivWrite;
        ImageView ivReceive;
        TextView tvValue;
        ImageView ivRead;
    }

    //写入可读的Characteristic字段的监听
    private void readCharacteristic(UUID serviceUUID, UUID characterUUID, final TextView textView) {
        DeviceConnectionActivity.mClient.read(DeviceConnectionActivity.macStr, serviceUUID, characterUUID, new BleReadResponse() {
            @Override
            public void onResponse(int code, byte[] data) {
                if (code == REQUEST_SUCCESS) {
                    String s = HexUtil.encodeHexStr(data);
                    String s1 = hexStringToString(s);
                    textView.setText(s1);
                } else {
                    ToastUtil.showShortToast(context, "读取失败");
                }
            }
        });
    }

}
