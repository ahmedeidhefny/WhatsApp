package com.ahmed.eid.whatsapp;

import android.content.Intent;
import android.os.TestLooperManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {

    RecyclerView mFindFriendsRecyclerList;
    DatabaseReference mUserRef;
    Toolbar mToolbar;
    public static final String friendIdKey = "friend_id";
    private FirebaseAuth mAuth;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        mToolbar = findViewById(R.id.find_friends_toolbar);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("FindFriends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        mUserId = mAuth.getCurrentUser().getUid();
        mFindFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mFindFriendsRecyclerList.setLayoutManager(manager);


    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users");

        FirebaseRecyclerOptions<ContactModel> options = new
                FirebaseRecyclerOptions.Builder<ContactModel>()
                .setQuery(query, new SnapshotParser<ContactModel>() {
                    @NonNull
                    @Override
                    public ContactModel parseSnapshot(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("user_image")) {
                            return new ContactModel(
                                    snapshot.child("user_image").getValue().toString(),
                                    snapshot.child("user_name").getValue().toString(),
                                    snapshot.child("user_status").getValue().toString());
                        } else {
                            return new ContactModel(
                                    snapshot.child("user_name").getValue().toString(),
                                    snapshot.child("user_status").getValue().toString());
                        }
                    }
                })
                .build();

        FirebaseRecyclerAdapter<ContactModel, FindFriendsViewHolder> adapter = new
                FirebaseRecyclerAdapter<ContactModel, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(FindFriendsViewHolder holder, final int position, ContactModel model) {
                        //String mFriendId = getRef(position).getKey().toString();
                        //if (!mUserId.equals(mFriendId)) {
                        holder.mFriendName.setText(model.getName());
                        holder.mFriendStatus.setText(model.getStatus());
                        Picasso.get()
                                .load(model.getImage())
                                .placeholder(R.drawable.unknown_user)
                                .error(R.drawable.unknown_user)
                                .into(holder.mFriendImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String mFriendId = getRef(position).getKey().toString();
                                Intent friendProfileIntent = new Intent(FindFriendsActivity.this, FriendProfileActivity.class);
                                friendProfileIntent.putExtra(friendIdKey, mFriendId);
                                startActivity(friendProfileIntent);
                            }
                        });

                        //}
                    }

                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View myView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_find_friends_list, parent, false);
                        return new FindFriendsViewHolder(myView);
                    }
                };

        mFindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {

        TextView mFriendName, mFriendStatus;
        CircularImageView mFriendImage, mFriendOnline;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mFriendName = itemView.findViewById(R.id.find_friends_user_name);
            mFriendStatus = itemView.findViewById(R.id.find_friends_user_status);
            mFriendOnline = itemView.findViewById(R.id.find_friends_user_online);
            mFriendImage = itemView.findViewById(R.id.find_friends_user_image);
        }
    }

}


