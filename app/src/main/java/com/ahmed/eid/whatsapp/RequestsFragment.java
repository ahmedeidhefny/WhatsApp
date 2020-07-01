package com.ahmed.eid.whatsapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;


public class RequestsFragment extends Fragment {

    View mRequestsFragmentView;
    RecyclerView mRequestsRecycler;
    private DatabaseReference mUserRef, mContactsRef, mRequestsRef;
    private FirebaseAuth mAuth;
    public static final String friendIdKey = "friend_id";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);
        mRequestsRecycler = mRequestsFragmentView.findViewById(R.id.requests_recycler);
        mRequestsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        mContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();

        return mRequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = mRequestsRef.child(mAuth.getCurrentUser().getUid());
        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions
                .Builder<String>()
                .setQuery(query, new SnapshotParser<String>() {
                    @NonNull
                    @Override
                    public String parseSnapshot(@NonNull DataSnapshot snapshot) {
                        return snapshot.getKey();
                    }
                })
                .build();

        FirebaseRecyclerAdapter<String, RequestContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<String, RequestContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestContactsViewHolder holder, final int position, @NonNull final String contactId) {
                        DatabaseReference mAllRequestsRef = getRef(position).child("request_type").getRef();
                        mAllRequestsRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if ((dataSnapshot.exists())) {
                                    String type = dataSnapshot.getValue().toString();
                                    if (type.equals("receiver")) {
                                        holder.requestCancelBtn.setVisibility(View.VISIBLE);
                                        holder.requestCancelBtn.setEnabled(true);
                                        holder.requestAcceptBtn.setVisibility(View.VISIBLE);
                                        holder.requestAcceptBtn.setEnabled(true);
                                    } else if (type.equals("sender")) {
                                        holder.requestAcceptBtn.setVisibility(View.GONE);
                                        holder.requestAcceptBtn.setEnabled(false);
                                        holder.requestCancelBtn.setVisibility(View.VISIBLE);
                                        holder.requestCancelBtn.setEnabled(true);
                                        holder.requestCancelBtn.setText("Cancel Reqest");
                                    }

                                    mUserRef.child(contactId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {

                                                if (dataSnapshot.hasChild("user_image")) {

                                                    String contactImage = dataSnapshot.child("user_image").getValue().toString();
                                                    Picasso.get()
                                                            .load(contactImage)
                                                            .placeholder(R.drawable.unknown_user)
                                                            .error(R.drawable.unknown_user)
                                                            .into(holder.mFriendImage);

                                                }
                                                String contactStatus = dataSnapshot.child("user_status").getValue().toString();
                                                String contactName = dataSnapshot.child("user_name").getValue().toString();

                                                holder.mFriendName.setText(contactName);
                                                holder.mFriendStatus.setText(contactStatus);
                                            } else {
                                                Toast.makeText(getContext(), "No Data or An Error", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError
                                                                        databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent friendProfileIntent = new Intent(getContext(), FriendProfileActivity.class);
                                friendProfileIntent.putExtra(friendIdKey, contactId);
                                getContext().startActivity(friendProfileIntent);
                            }
                        });

                        holder.requestAcceptBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mContactsRef.child(mAuth.getCurrentUser().getUid()).child(contactId)
                                        .setValue("myFriend").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mContactsRef.child(contactId).child(mAuth.getCurrentUser().getUid())
                                                    .setValue("myFriend").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        cancelRequest(contactId);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                            }
                        });

                        holder.requestCancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelRequest(contactId);
                            }
                        });


                    }

                    @NonNull
                    @Override
                    public RequestContactsViewHolder onCreateViewHolder(@NonNull ViewGroup
                                                                                viewGroup, int i) {
                        View myView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_find_friends_list, viewGroup, false);
                        return new RequestContactsViewHolder(myView);
                    }
                };
        mRequestsRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    private void cancelRequest(final String contactID) {
        mRequestsRef.child(mAuth.getCurrentUser().getUid()).child(contactID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mRequestsRef.child(contactID).child(mAuth.getCurrentUser().getUid())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                            }
                        }
                    });
                }
            }
        });
    }

    public class RequestContactsViewHolder extends RecyclerView.ViewHolder {

        protected TextView mFriendName, mFriendStatus;
        protected CircularImageView mFriendImage, mFriendOnline;
        protected Button requestAcceptBtn, requestCancelBtn;

        public RequestContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            mFriendName = itemView.findViewById(R.id.find_friends_user_name);
            mFriendStatus = itemView.findViewById(R.id.find_friends_user_status);
            mFriendOnline = itemView.findViewById(R.id.find_friends_user_online);
            mFriendImage = itemView.findViewById(R.id.find_friends_user_image);
            requestAcceptBtn = itemView.findViewById(R.id.request_accept_btn);
            requestCancelBtn = itemView.findViewById(R.id.request_cancel_btn);
        }


    }
}
