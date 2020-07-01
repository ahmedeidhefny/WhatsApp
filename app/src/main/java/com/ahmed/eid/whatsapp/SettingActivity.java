package com.ahmed.eid.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.profile_image)
    CircularImageView profileImage;
    @BindView(R.id.user_name)
    EditText userName;
    @BindView(R.id.user_status)
    EditText userStatus;
    @BindView(R.id.user_update_button)
    Button userUpdateButton;
    private Uri imageUri;

    Toolbar toolbar;

    DatabaseReference mRootRef;
    StorageReference mRootStore;
    FirebaseAuth mAuth;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootStore = FirebaseStorage.getInstance().getReference();
        loadingBar = new ProgressDialog(this);

        toolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        getUserInfo();
    }

    private void getUserInfo() {
        mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("user_image")) {
                        String mUserImage = dataSnapshot.child("user_image").getValue().toString();
                        Picasso.get().load(mUserImage)
                                .error(R.drawable.unknown_user)
                                .placeholder(R.drawable.unknown_user)
                                .into(profileImage);

                    }
                    String mUserName = dataSnapshot.child("user_name").getValue().toString();
                    String mUserStatus = dataSnapshot.child("user_status").getValue().toString();

                    userName.setText(mUserName);
                    userStatus.setText(mUserStatus);
                } else {
                    Toast.makeText(SettingActivity.this, "please, set or update your profile information...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @OnClick({R.id.profile_image, R.id.user_update_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.profile_image:
                updateProfileImage();
                break;
            case R.id.user_update_button:
                updateProfile();
                break;
        }
    }

    private void updateProfileImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                loadingBar.setTitle("Uploading image..");
                loadingBar.setMessage("please Waiting, while uploading image to backend..!");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final StorageReference filePath = mRootStore.child("Profile Images").child(mAuth.getCurrentUser().getUid() + ".jpg");
                filePath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Toast.makeText(SettingActivity.this, "image Uploaded successfully..!", Toast.LENGTH_SHORT).show();
                            mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("user_image").setValue(downloadUri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Toast.makeText(SettingActivity.this, "image Uploaded&Stored successfully..!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loadingBar.dismiss();
                                        String error = task.getException().toString();
                                        Toast.makeText(SettingActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            loadingBar.dismiss();
                            String error = task.getException().toString();
                            Toast.makeText(SettingActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }
    }

    private void updateProfile() {

        if (validation()) {
            String name = userName.getText().toString();
            String status = userStatus.getText().toString();
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("user_name", name);
            profileMap.put("user_status", status);
            mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SettingActivity.this, "profile update successfully..", Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        String massage = task.getException().toString();
                        Toast.makeText(SettingActivity.this, "" + massage, Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }

    }

    private void goToMainActivity() {
        Intent mainIntent = new Intent(SettingActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }

    private Boolean validation() {
        String name = userName.getText().toString();
        String status = userStatus.getText().toString();

        if (TextUtils.isEmpty(name)) {
            userName.setError("Please, Enter your Name..");
            return false;
        }
        if (TextUtils.isEmpty(status)) {
            userStatus.setError("please, Enter your status..");
            return false;
        }
        return true;
    }
}
