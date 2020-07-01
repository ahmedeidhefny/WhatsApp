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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;


public class ChatsFragment extends Fragment {

    View myChatView;
    RecyclerView mChatsRecyclerView;
    DatabaseReference mContactsRef, mUserRef;
    FirebaseAuth mAuthn;

    public final static String mContactId = "contact_id";
    public final static String mContactName = "contact_name";
    public final static String mContactImage = "contact_image";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myChatView = inflater.inflate(R.layout.fragment_chats, container, false);
        mAuthn = FirebaseAuth.getInstance();
        mContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mChatsRecyclerView = myChatView.findViewById(R.id.chats_recycler);
        mChatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return myChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = mContactsRef.child(mAuthn.getCurrentUser().getUid());
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

        FirebaseRecyclerAdapter<String, ChatViewHolder> adapter =
                new FirebaseRecyclerAdapter<String, ChatViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull final String contactId) {
                        mUserRef.child(contactId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String contactImage = null;
                                    if (dataSnapshot.hasChild("user_image")) {
                                        contactImage = dataSnapshot.child("user_image").getValue().toString();
                                        Picasso.get()
                                                .load(contactImage)
                                                .placeholder(R.drawable.unknown_user)
                                                .error(R.drawable.unknown_user)
                                                .into(holder.mFriendImage);
                                    }

                                    final String contactStatus = dataSnapshot.child("user_status").getValue().toString();
                                    final String contactName = dataSnapshot.child("user_name").getValue().toString();

                                    holder.mFriendName.setText(contactName);
                                    holder.mFriendStatus.setText("Last Seen: " + "\n" + "Date " + "Time");

                                    final String finalContactImage = contactImage;
                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(getContext(), InboxChatActivity.class);
                                            intent.putExtra(mContactId, contactId);
                                            intent.putExtra(mContactName, contactName);
                                            intent.putExtra(mContactImage, finalContactImage);
                                            startActivity(intent);
                                        }
                                    });

                                    if (dataSnapshot.child("userStatus").hasChild("status")) {

                                        String status = dataSnapshot.child("userStatus").child("status").getValue().toString();
                                        String date = dataSnapshot.child("userStatus").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userStatus").child("time").getValue().toString();

                                        if (status.equals("online")) {
                                            holder.mFriendStatus.setText("Online Now");
                                            holder.mFriendOnline.setVisibility(View.VISIBLE);

                                        } else if (status.equals("offline")) {
                                            holder.mFriendStatus.setText("Last Seen: " + "\n" + date + " " + time);
                                            holder.mFriendOnline.setVisibility(View.INVISIBLE);
                                        }

                                    } else {
                                        holder.mFriendStatus.setText("Offline");
                                        holder.mFriendOnline.setVisibility(View.INVISIBLE);
                                    }

                                } else {
                                    Toast.makeText(getContext(), "No Data or An Error", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View myView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_find_friends_list, viewGroup, false);
                        return new ChatViewHolder(myView);
                    }
                };

        mChatsRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        protected TextView mFriendName, mFriendStatus;
        protected CircularImageView mFriendImage, mFriendOnline;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            mFriendName = itemView.findViewById(R.id.find_friends_user_name);
            mFriendStatus = itemView.findViewById(R.id.find_friends_user_status);
            mFriendOnline = itemView.findViewById(R.id.find_friends_user_online);
            mFriendImage = itemView.findViewById(R.id.find_friends_user_image);
        }
    }
}
