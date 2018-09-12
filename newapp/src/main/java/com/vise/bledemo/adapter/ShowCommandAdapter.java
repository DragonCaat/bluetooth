package com.vise.bledemo.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.vise.bledemo.R;
import com.vise.bledemo.common.OnCommandClickListener;
import com.vise.bledemo.common.ToastUtil;
import com.vise.bledemo.database.RecordDatabaseUtils;

import java.util.List;

/**
 * 显示历史命令的适配器
 */
public class ShowCommandAdapter extends BaseAdapter {

    private List<String> list;

    private OnCommandClickListener commandClickListener;

    public ShowCommandAdapter(List<String> list) {
        this.list = list;
        //this.editText = editText;
    }

    /**
     * 设置监听
     * @param commandClickListener
     */
    public void setOnItemClickLitener(OnCommandClickListener commandClickListener) {
        this.commandClickListener = commandClickListener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View convertView, final ViewGroup viewGroup) {
        View view;
        if (convertView != null)
            view = convertView;
        else
            view = View.inflate(viewGroup.getContext(), R.layout.command_item, null);

        final TextView viewById = view.findViewById(R.id.tv_out_item);
        ImageView ivDelete = view.findViewById(R.id.iv_delete);

        final CheckBox checkBox = view.findViewById(R.id.cb);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkBox.setChecked(isChecked);
            }
        });

        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordDatabaseUtils.deleteDataByName(list.get(i));
                list.remove(list.get(i));
                notifyDataSetChanged();
            }
        });

        viewById.setText(list.get(i));
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置点击事件 具体的调用执行，由当前的adapter实例进行操作
                commandClickListener.onItemClick(viewById, i);

            }
        });
        return view;
    }



}
