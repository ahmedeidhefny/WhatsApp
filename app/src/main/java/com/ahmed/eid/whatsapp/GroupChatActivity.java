package com.ahmed.eid.whatsapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.internal.Utils;

public class GroupChatActivity extends AppCompatActivity {

    @BindView(R.id.include_main_toolbar)
    Toolbar mainAppToolbar;
    @BindView(R.id.write_comment_et)
    EditText writeCommentEt;
    @BindView(R.id.send_massage_btn)
    ImageButton sendMassageBtn;
    @BindView(R.id.text_massage)
    TextView displayTextMassage;
    @BindView(R.id.scroll_view)
    android.widget.ScrollView mScrollView;

    FirebaseAuth mAuth ;
    DatabaseReference mUsersRef, mGroupNameRef, mGroupMassageKeyRef;

    private String mMassageKey, mGroupName, currentUserId,currentUserName,
                   mUserMassage, currentDate, currentTime ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        ButterKnife.bind(this);
        intializeToolBar();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mGroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(mGroupName);
        getUserNameInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayGroupChat();
        mScrollView.fullScroll(android.widget.ScrollView.FOCUS_DOWN);
    }


    private void intializeToolBar() {
        mGroupName = getIntent().getStringExtra(GroupsFragment.GROUP_NAME_KEY);
        setSupportActionBar(mainAppToolbar);
        getSupportActionBar().setTitle(mGroupName);

    }

    private void getUserNameInfo() {
        mUsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists()){
                   currentUserName = dataSnapshot.child("user_name").getValue().toString();
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayGroupChat() {
        mGroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayTextMassages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayTextMassages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayTextMassages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String massageDate =((DataSnapshot)iterator.next()).getValue().toString();
            String massage =((DataSnapshot)iterator.next()).getValue().toString();
            String massageTime =((DataSnapshot)iterator.next()).getValue().toString();
            String massageUserName =((DataSnapshot)iterator.next()).getValue().toString();

            displayTextMassage.append(massageUserName+ " :\n" + massage + "\n" + massageDate + "  " + massageTime + "\n\n\n");
            mScrollView.fullScroll(android.widget.ScrollView.FOCUS_DOWN);
        }

    }

    @OnClick(R.id.send_massage_btn)
    public void onViewClicked() {
        addMassageToDB();
        writeCommentEt.setText(" ");
        mScrollView.fullScroll(android.widget.ScrollView.FOCUS_DOWN);
    }

    private void addMassageToDB() {
        mUserMassage = writeCommentEt.getText().toString();
        if (TextUtils.isEmpty(mUserMassage) || mUserMassage == null || mUserMassage.equals(" ")){
            Toast.makeText(this, "Write Massage..", Toast.LENGTH_SHORT).show();
            return;
        }else {
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy");
            currentDate = dateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = timeFormat.format(calendarTime.getTime());

            HashMap<String, Object> mGroupNameHash = new HashMap<>();
            mGroupNameRef.updateChildren(mGroupNameHash);

            mMassageKey = mGroupNameRef.push().getKey().toString();
            mGroupMassageKeyRef = mGroupNameRef.child(mMassageKey);

            HashMap<String,Object> mGroupMassageHash = new HashMap<>();
            mGroupMassageHash.put("user_name",currentUserName);
            mGroupMassageHash.put("massage",mUserMassage);
            mGroupMassageHash.put("date",currentDate);
            mGroupMassageHash.put("time",currentTime);
            mGroupMassageKeyRef.updateChildren(mGroupMassageHash);
        }
    }
}
