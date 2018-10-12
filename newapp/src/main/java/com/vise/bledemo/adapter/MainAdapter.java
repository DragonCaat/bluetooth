package com.vise.bledemo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vise.bledemo.R;
import com.vise.bledemo.bean.MainBean;

import java.util.List;

/**
 * @author : Darcy
 * @Date ${Date}
 * @Description 首页的连接蓝牙的设备
 */
public class MainAdapter extends BaseAdapter {

    private List<MainBean> list;
    private Context context;

    public MainAdapter(List<MainBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null)
            view = convertView;
        else
            view = View.inflate(context, R.layout.main_item, null);

        TextView mac = view.findViewById(R.id.tv_mac);

        mac.setText(list.get(position).getName());

        TextView name = view.findViewById(R.id.tv_name);
        name.setText(list.get(position).getMacStr());
        return view;
    }
}
