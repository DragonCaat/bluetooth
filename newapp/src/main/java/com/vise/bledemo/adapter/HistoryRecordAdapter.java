package com.vise.bledemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vise.bledemo.R;
import com.vise.bledemo.bean.HistoryRecordEntity;

import java.util.List;

/**
 * 历史操作记录
 *
 * @author Darcy
 */
public class HistoryRecordAdapter extends RecyclerView.Adapter<HistoryRecordAdapter.MyViewHolder> {


    private List<HistoryRecordEntity> list;
    private Context context;

    public HistoryRecordAdapter(List<HistoryRecordEntity> list, Context context) {
        this.list = list;
        this.context = context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.history_record_item,null);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        HistoryRecordEntity recordEntity = list.get(position);

        holder.tvTime.setText(recordEntity.getTime());
        holder.tvDes.setText(recordEntity.getDes());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTime;
        TextView tvDes;
        public MyViewHolder(View itemView) {
            super(itemView);

            tvTime = itemView.findViewById(R.id.tv_time);
            tvDes =itemView.findViewById(R.id.tv_des);
        }
    }
}
