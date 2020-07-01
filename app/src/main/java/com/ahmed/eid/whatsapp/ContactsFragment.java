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
import android.widget.Button;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    View mContactsFragmentView;
    RecyclerView mContactsRecycler;
    FirebaseAuth mAuth;
    DatabaseReference mUserRef;

    public static final String friendIdKey = "friend_id";

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContactsFragmentView = inflater.inflate(R.layout.fragment_contacts, container, false);
        mAuth = FirebaseAuth.getInstance();
        mContactsRecycler = mContactsFragmentView.findViewById(R.id.contacts_recycler);
        mContactsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return mContactsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = FirebaseDatabase.getInstance().getReference().child("Contacts").child(mAuth.getCurrentUser().getUid());
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

        FirebaseRecyclerAdapter<String, ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<String, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull final String contactId) {

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

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent friendProfileIntent = new Intent(getContext(), FriendProfileActivity.class);
                                            friendProfileIntent.putExtra(friendIdKey, contactId);
                                            getContext().startActivity(friendProfileIntent);
                                        }
                                    });

                                    if (dataSnapshot.child("userStatus").hasChild("status")) {

                                        String status = dataSnapshot.child("userStatus").child("status").getValue().toString();
                                        if (status.equals("online")) {
                                            holder.mFriendOnline.setVisibility(View.VISIBLE);

                                        } else if (status.equals("offline")) {
                                            holder.mFriendOnline.setVisibility(View.INVISIBLE);
                                        }

                                    } else {
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
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View myView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_find_friends_list, viewGroup, false);
                        return new ContactsViewHolder(myView);
                    }
                };

        mContactsRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {

        protected TextView mFriendName, mFriendStatus;
        protected CircularImageView mFriendImage, mFriendOnline;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            mFriendName = itemView.findViewById(R.id.find_friends_user_name);
            mFriendStatus = itemView.findViewById(R.id.find_friends_user_status);
            mFriendOnline = itemView.findViewById(R.id.find_friends_user_online);
            mFriendImage = itemView.findViewById(R.id.find_friends_user_image);
        }
    }
}
