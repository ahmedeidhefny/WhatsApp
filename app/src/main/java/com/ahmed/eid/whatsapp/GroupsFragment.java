package com.ahmed.eid.whatsapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GroupsFragment extends Fragment  implements GroupsListAdapter.OnItemClickListener{

    View mView = null;
    DatabaseReference mRootRef ;
    GroupsListAdapter mGroupsListAdapter ;
    ArrayList<String> mGroupList;
    RecyclerView mGroupsListRecycler;
    public final static String GROUP_NAME_KEY = "GroupName";


    public GroupsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_groups, container, false);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mGroupList = new ArrayList<String>();
        mGroupsListRecycler = mView.findViewById(R.id.list_groups_recycler);
        setRecyclerViewWithAdapter();
        retrieveAndDisplayGroups();
        return mView;
    }

    private void setRecyclerViewWithAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        mGroupsListRecycler.setLayoutManager(linearLayoutManager);
        mGroupsListAdapter = new GroupsListAdapter(getActivity(),mGroupList);
        mGroupsListAdapter.setOnItemClickListener(this);
        mGroupsListRecycler.setAdapter(mGroupsListAdapter);
    }

    private void retrieveAndDisplayGroups() {
        mRootRef.child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                if (dataSnapshot.exists()){
                    Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()){
                        set.add(((DataSnapshot)iterator.next()).getKey());
                    }
                    mGroupList.clear();
                    mGroupList.addAll(set);
                    mGroupsListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    @Override
    public void itemSelected(String mGroupName) {
        Intent mGroupChatIntent = new Intent(getActivity(), GroupChatActivity.class);
        mGroupChatIntent.putExtra(GROUP_NAME_KEY,mGroupName);
        startActivity(mGroupChatIntent);
    }
}
