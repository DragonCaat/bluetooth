package com.vise.bledemo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vise.bledemo.R;
import com.vise.bledemo.bean.BluetoothEntity;
import com.vise.bledemo.common.OnRecycleViewItemClickListener;

import java.util.List;

import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET;
import static android.bluetooth.BluetoothClass.Device.COMPUTER_LAPTOP;
import static android.bluetooth.BluetoothClass.Device.PHONE_SMART;
import static android.bluetooth.BluetoothClass.Device.WEARABLE_WRIST_WATCH;

/**
 * 搜索蓝牙的适配器
 *
 * @author Darcy
 */
public class SearchBluetoothAdapter extends RecyclerView.Adapter<SearchBluetoothAdapter.ViewHolder> implements View.OnClickListener {

    private int CONNECTED = 1;//已经配对的蓝牙

    private int CONNECTING = 0;//未配对的蓝牙

    private List<BluetoothEntity> list;
    private Context context;

    //声明自定义的监听接口
    private OnRecycleViewItemClickListener mOnRecycleviewItemClickListener = null;

    //构造方法中添加自定义监听接口
    public SearchBluetoothAdapter(Context context, List<BluetoothEntity> list, OnRecycleViewItemClickListener mOnRecycleviewItemClickListener) {
        this.context = context;
        this.list = list;
        this.mOnRecycleviewItemClickListener = mOnRecycleviewItemClickListener;
    }

    //创建View,被LayoutManager所用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_show_bluetooth_item, parent, false);
        //这里 我们可以拿到点击的item的view 对象，所以在这里给view设置点击监听，
        view.setOnClickListener(this);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    //数据的绑定
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        BluetoothEntity bluetoothEntity = list.get(position);

        holder.tvName.setText(bluetoothEntity.getName());
        holder.tvAddress.setText(bluetoothEntity.getAddress());
        if (bluetoothEntity.getConnection() == 1)
            holder.tvConnection.setText("已配对");
        else
            holder.tvConnection.setText("");

        if (bluetoothEntity.getType() == PHONE_SMART)
            holder.ivType.setImageResource(R.mipmap.phone);//智能手机
        else if (bluetoothEntity.getType() == COMPUTER_LAPTOP)
            holder.ivType.setImageResource(R.mipmap.computer);//笔记本
        else if (bluetoothEntity.getType() == AUDIO_VIDEO_WEARABLE_HEADSET)
            holder.ivType.setImageResource(R.mipmap.bluetooth_earphone);//蓝牙耳机
        else if (bluetoothEntity.getType() == WEARABLE_WRIST_WATCH)
            holder.ivType.setImageResource(R.mipmap.watch_small);//手表
        else if (bluetoothEntity.getType() == 7936)
            holder.ivType.setImageResource(R.mipmap.band);//手环
        else
            holder.ivType.setImageResource(R.mipmap.bluetooth);

        Log.i("hello", "onBindViewHolder: " + bluetoothEntity.getType()+bluetoothEntity.getAddress());

        holder.tvDistance.setText(bluetoothEntity.getDistance() + " m");

        holder.tvSignal.setText("" + bluetoothEntity.getRssi());

        holder.itemView.setTag(position);//给view设置tag以作为参数传递到监听回调方法中
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onClick(View view) {
        //将监听传递给自定义接口
        mOnRecycleviewItemClickListener.onItemClickListener(view, ((int) view.getTag()));
    }

    //自定义ViewHolder,包含item的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAddress;
        TextView tvConnection;
        TextView tvDistance;
        TextView tvSignal;

        ImageView ivType;

        @SuppressLint("CutPasteId")
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvConnection = itemView.findViewById(R.id.tv_connection);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvSignal = itemView.findViewById(R.id.tv_signal);

            ivType = itemView.findViewById(R.id.iv_type);
        }
    }

    //自定义ViewHolder,包含item的所有界面元素
    public static class HeadViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAddress;

        @SuppressLint("CutPasteId")
        public HeadViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAddress = itemView.findViewById(R.id.tv_address);

        }
    }
}


