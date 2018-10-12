package com.vise.bledemo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.vise.bledemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : Darcy
 * @Date ${Date}
 * @Description 分析数据的适配器
 */
public class AnalysisAdapter extends BaseExpandableListAdapter {

    private List<String> groupStrings;

    private List<List<String>> childStrings;

    private Context mContext;

    public AnalysisAdapter(List<String> groupStrings, List<List<String>> childStrings, Context mContext){
        this.childStrings=childStrings;
        this.groupStrings = groupStrings;
        this.mContext = mContext;
    }

    //获取分组的个数
    @Override
    public int getGroupCount() {
        return groupStrings.size();
    }

    //获取指定分组中的子选项的个数
    @Override
    public int getChildrenCount(int groupPosition) {
        return childStrings.get(groupPosition).size();
    }

    // 获取指定的分组数据
    @Override
    public Object getGroup(int groupPosition) {
        return groupStrings.get(groupPosition);
    }

    // 获取指定分组中的指定子选项数据
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childStrings.get(groupPosition).get(childPosition);
    }

    //获取指定分组的ID, 这个ID必须是唯一的
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    // 获取子选项的ID, 这个ID必须是唯一的
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    //分组和子选项是否持有稳定的ID, 就是说底层数据的改变会不会影响到它们。
    @Override
    public boolean hasStableIds() {
        return true;
    }

    // 获取显示指定分组的视图
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_expand_group, parent, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.tvTitle = convertView.findViewById(R.id.label_expand_group);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        groupViewHolder.tvTitle.setText(groupStrings.get(groupPosition));
        return convertView;
    }

    // 获取显示指定分组中的指定子选项的视图
    @SuppressLint("SetTextI18n")
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_expand_child, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.tvTitle = convertView.findViewById(R.id.label_expand_child);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        String label = "0"+"--"+5*(childPosition+1)+"分钟的步数 :";

        childViewHolder.tvTitle.setText(label+childStrings.get(groupPosition).get(childPosition));
        return convertView;
    }

    //指定位置上的子元素是否可选中
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class GroupViewHolder {
        TextView tvTitle;
    }

    static class ChildViewHolder {
        TextView tvTitle;
    }
}
