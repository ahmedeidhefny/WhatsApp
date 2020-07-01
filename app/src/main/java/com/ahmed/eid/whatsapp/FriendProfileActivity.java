package com.ahmed.eid.whatsapp;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.internal.Utils;

public class FriendProfileActivity extends AppCompatActivity {

    String mReceiverId, mSenderId;
    String currentState;
    private DatabaseReference mUserRef, mChatRequests, mContacts, mNotifications;
    private FirebaseAuth mAuth;
    CircularImageView userCircleImage;
    TextView userNameText, userStatusText;
    Button mFollowBtn, mCancelBtn;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firend_profile);

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mChatRequests = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        mContacts = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mNotifications = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mAuth = FirebaseAuth.getInstance();

        userCircleImage = findViewById(R.id.profile_user_image);
        userNameText = findViewById(R.id.profile_user_name);
        userStatusText = findViewById(R.id.profile_user_status);
        mFollowBtn = findViewById(R.id.profile_follow_btn);
        mCancelBtn = findViewById(R.id.profile_cancel_btn);

        String mFriendId = getIntent().getStringExtra(FindFriendsActivity.friendIdKey);
        if (TextUtils.isEmpty(mFriendId)) {
            finish();
        } else {
            mReceiverId = mFriendId;
            //Toast.makeText(this, ""+mReceiverId, Toast.LENGTH_SHORT).show();
        }

        mSenderId = mAuth.getCurrentUser().getUid();
        currentState = "new";

        getUserData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mChatRequests.child(mSenderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild(mReceiverId)) {
                    String request_type = dataSnapshot.child(mReceiverId).child("request_type").getValue().toString();
                    if (request_type.equals("sender")) {
                        mFollowBtn.setEnabled(true);
                        mFollowBtn.setText("Cancel Request");
                        currentState = "send_request";
                    } else if (request_type.equals("receiver")) {
                        mFollowBtn.setEnabled(true);
                        mFollowBtn.setText("Accept Request");
                        currentState = "receive_request";
                        mCancelBtn.setVisibility(View.VISIBLE);
                        mCancelBtn.setEnabled(true);
                        mCancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelRequest();
                            }
                        });
                    }
                } else {
                    mContacts.child(mSenderId).child(mReceiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mFollowBtn.setEnabled(true);
                                mFollowBtn.setText("UnFollow");
                                currentState = "friends";
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void getUserData() {
        mUserRef.child(mReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("user_image"))) {
                    String userimage = dataSnapshot.child("user_image").getValue().toString();
                    String userName = dataSnapshot.child("user_name").getValue().toString();
                    String userStatus = dataSnapshot.child("user_status").getValue().toString();

                    userNameText.setText(userName);
                    userStatusText.setText(userStatus);
                    Picasso.get()
                            .load(userimage)
                            .placeholder(R.drawable.unknown_user)
                            .error(R.drawable.unknown_user)
                            .into(userCircleImage);
                    handleCLickFollow();
                } else if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("user_name").getValue().toString();
                    String userStatus = dataSnapshot.child("user_status").getValue().toString();

                    userNameText.setText(userName);
                    userStatusText.setText(userStatus);

                    handleCLickFollow();
                } else {
                    Toast.makeText(FriendProfileActivity.this, "NoData or accoured an error..!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void handleCLickFollow() {

        if (!mSenderId.equals(mReceiverId)) {
            mFollowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentState.equals("new")) {
                        mFollowBtn.setEnabled(false);
                        addNewRequest();
                    } else if (currentState.equals("send_request")) {
                        mFollowBtn.setEnabled(false);
                        cancelRequest();
                    } else if (currentState.equals("receive_request")) {
                        mFollowBtn.setEnabled(false);
                        addNewContact();
                    } else if (currentState.equals("friends")) {
                        mFollowBtn.setEnabled(false);
                        cancelContact();
                    }

                }
            });

        } else {
            mFollowBtn.setEnabled(false);
            mFollowBtn.setVisibility(View.INVISIBLE);
        }

    }

    private void addNewRequest() {
        mChatRequests.child(mSenderId).child(mReceiverId)
                .child("request_type").setValue("sender").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mChatRequests.child(mReceiverId).child(mSenderId)
                            .child("request_type").setValue("receiver").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                HashMap<String, Object> mNotificationMap = new HashMap();
                                mNotificationMap.put("from", mSenderId);
                                mNotificationMap.put("to", mReceiverId);
                                mNotificationMap.put("type", "request");

                                mNotifications.child(mReceiverId).push()
                                        .updateChildren(mNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mFollowBtn.setText("Cancel Request");
                                            mFollowBtn.setEnabled(true);
                                            currentState = "send_request";
                                        }
                                    }
                                });
                            }
                        }
                    });
                }

            }
        });

    }

    private void cancelRequest() {
        mChatRequests.child(mSenderId).child(mReceiverId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mChatRequests.child(mReceiverId).child(mSenderId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            currentState = "new";
                            mFollowBtn.setText("Follow");
                            mFollowBtn.setEnabled(true);

                            mCancelBtn.setEnabled(false);
                            mCancelBtn.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
    }

    private void addNewContact() {
        mContacts.child(mSenderId).child(mReceiverId).setValue("myFriend").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mContacts.child(mReceiverId).child(mSenderId).setValue("myFriend").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                cancelRequestAfterAccepted();
                            }

                        }
                    });
                }
            }
        });
    }

    private void cancelRequestAfterAccepted() {
        mChatRequests.child(mSenderId).child(mReceiverId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mChatRequests.child(mReceiverId).child(mSenderId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFollowBtn.setEnabled(true);
                            mFollowBtn.setText("UnFollow");
                            currentState = "friends";

                            mCancelBtn.setEnabled(false);
                            mCancelBtn.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
    }

    private void cancelContact() {

        mContacts.child(mSenderId).child(mReceiverId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mContacts.child(mReceiverId).child(mSenderId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mFollowBtn.setEnabled(true);
                                mFollowBtn.setText("Follow");
                                currentState = "new";
                            }
                        }
                    });
                }
            }
        });

    }

}
