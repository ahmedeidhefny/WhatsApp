package com.ahmed.eid.whatsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Toolbar mToolbar;
    ViewPager myViewPager;
    TabLayout myTabLayout;
    TabAccessorAdapter myTabAccessorAdapter;
    private FirebaseAuth mAuth;
    String userId;
    private DatabaseReference mRootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        setTitle("What's App");

        myTabLayout = findViewById(R.id.main_tabs);
        myTabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager());

        myViewPager = findViewById(R.id.main_view_pager);
        myViewPager.setAdapter(myTabAccessorAdapter);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser == null) {
            goToLoginActivity();
        } else {
            verifyUserExistance();
            updateStatus("online");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser != null) {
            updateStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser != null) {
            updateStatus("offline");
        }
    }

    private void verifyUserExistance() {
        mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("user_name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(MainActivity.this, "Welcome..", Toast.LENGTH_SHORT).show();
                } else {
                    goToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String massage = databaseError.getMessage();
                Toast.makeText(MainActivity.this, "" + massage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.option_find_friends_menu:
                goToFindFriendsActivity();
                break;
            case R.id.option_create_group_menu:
                showMassageCreateGroup();
                break;
            case R.id.option_settings_menu:
                Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.option_logout_menu:
                mAuth.signOut();
                updateStatus("offline");
                goToLoginActivity();
                break;
        }
        return true;
    }

    private void showMassageCreateGroup() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Group.....");
        final EditText mGroupNameET = new EditText(this);
        mGroupNameET.setHint("Enter Here Group Name");
        mGroupNameET.setPadding(16, 16, 16, 16);
        builder.setView(mGroupNameET);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mGroupName = mGroupNameET.getText().toString();
                if (TextUtils.isEmpty(mGroupName)) {
                    mGroupNameET.setError("please, enter group Name..");
                } else {
                    createGroup(mGroupName);
                }
            }

        });
        builder.show();

    }

    private void createGroup(String mGroupName) {
        mRootRef.child("Groups").child(mGroupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Group is created successfully..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to create group..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void goToFindFriendsActivity() {
        Intent findFriendIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendIntent);
        finish();
    }

    private void goToSettingActivity() {
        Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(settingIntent);
    }


    private void updateStatus(String status) {

        String currentDateStatus, currentTimeStatus;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM,yyyy");
        currentDateStatus = dateFormat.format(calendar.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        currentTimeStatus = timeFormat.format(calendar.getTime());

        HashMap<String, Object> userStatusMap = new HashMap<>();
        userStatusMap.put("date", currentDateStatus);
        userStatusMap.put("time", currentTimeStatus);
        userStatusMap.put("status", status);

        mRootRef.child("Users").child(userId).child("userStatus")
                .updateChildren(userStatusMap);


    }
}
