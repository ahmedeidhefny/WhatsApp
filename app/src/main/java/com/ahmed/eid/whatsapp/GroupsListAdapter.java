package com.ahmed.eid.whatsapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GroupsListAdapter extends RecyclerView.Adapter<GroupsListAdapter.GroupsListViewHolder> {
    private Context mContext;
    private ArrayList<String> mGroupsList;
    private OnItemClickListener mOnItemClickListener ;

    public GroupsListAdapter(Context mContext, ArrayList<String> mGroupsList) {
        this.mContext = mContext;
        this.mGroupsList = mGroupsList;
    }

    public  void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @NonNull
    @Override
    public GroupsListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View myView = LayoutInflater.from(mContext).inflate(R.layout.item_groups_list, viewGroup, false);

        return new GroupsListViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsListViewHolder holder, int position) {
        final String mGroup = mGroupsList.get(position);
        holder.mGroupName.setText(mGroup);

        holder.mItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.itemSelected(mGroup);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mGroupsList.size();
    }

    public class GroupsListViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mItemLayout;
        TextView mGroupName;

        public GroupsListViewHolder(@NonNull View itemView) {
            super(itemView);
            mGroupName = itemView.findViewById(R.id.item_group_name);
            mItemLayout = itemView.findViewById(R.id.item_layout);
        }
    }

    public interface OnItemClickListener{
        void itemSelected(String mGroupName);
    }
}
