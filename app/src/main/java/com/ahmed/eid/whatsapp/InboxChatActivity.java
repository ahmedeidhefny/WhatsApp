package com.ahmed.eid.whatsapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class InboxChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView toolbarContactName, toolbarContactLast;
    EditText massageEt;
    ImageView sendTextBtn, uploadFileBtn;
    CircularImageView toolbarContactImage;
    FirebaseAuth mAuthn;
    DatabaseReference mRootRef;
    DatabaseReference mMassagesRef;
    StorageReference mStorageRootRef;

    private static final int IMAGE_REQUEST_CODE = 100;
    private static final int FILE_REQUEST_CODE = 200;

    String mReceiverId, mReceiverImage, mReceiverName, mSenderId, mChecker;

    InboxChatAdapter adapter;
    List<MassageModel> massages;
    RecyclerView massagesRecycler;

    Uri imageUri, imageDownloadedUri;
    String name;

    ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_chat);
        loadingBar =new ProgressDialog(this);
        mAuthn = FirebaseAuth.getInstance();
        mSenderId = mAuthn.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mMassagesRef = FirebaseDatabase.getInstance().getReference().child("Massages");
        mStorageRootRef = FirebaseStorage.getInstance().getReference();


        toolbar = findViewById(R.id.inbox_chat_toobar);
        massageEt = findViewById(R.id.inbox_write_comment_et);
        sendTextBtn = findViewById(R.id.inbox_send_massage_btn);
        uploadFileBtn = findViewById(R.id.inbox_upload_file_btn);

        customToolBar();
        getIntentData();

        massagesRecycler = findViewById(R.id.inbox_chat_recycler);
        massages = new ArrayList<>();
        massagesRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new InboxChatAdapter(massages);
        massagesRecycler.setAdapter(adapter);

        toolbarContactName.setText(mReceiverName);
        Picasso.get().load(mReceiverImage)
                .placeholder(R.drawable.unknown_user)
                .error(R.drawable.unknown_user)
                .into(toolbarContactImage);

        displayLastSeenToolbar();

        sendTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMassageText();
            }
        });

        uploadFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFiles();
            }
        });

    }

    private void uploadFiles() {

        CharSequence options[] = new CharSequence[]{
                "Image",
                "PDF File",
                "SM Word File"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select will Uploaded File!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    mChecker = "image";
                    selectAndUploadImage();
                }
                if (i == 1) {
                    mChecker = "pdf";
                }
                if (i == 2) {
                    mChecker = "word";
                }
            }
        });

        builder.show();
    }

    public void handleLoadingBar(String title, String massage){
        loadingBar.setTitle(title);
        loadingBar.setMessage(massage);
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

    }

    private void selectAndUploadImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_REQUEST_CODE && data.getData() != null) {
            imageUri = data.getData();
            UploadingImageToFirebaseStorage();
        }

    }

    private void UploadingImageToFirebaseStorage() {
        handleLoadingBar("Uploading Image..","Please Waiting, while to upload image to backend..");
        final StorageReference filePath = mStorageRootRef.child("MassagesImages").child(mSenderId + ".jpg");
        filePath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    imageDownloadedUri = task.getResult();
                    name = task.getResult().getLastPathSegment();
                    addImageRefToFirebase();
                }else {
                    loadingBar.dismiss();
                    Toast.makeText(InboxChatActivity.this, "Error, cannot upload image..!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void addImageRefToFirebase() {

        String currentDateStatus, currentTimeStatus;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM,yyyy");
        currentDateStatus = dateFormat.format(calendar.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        currentTimeStatus = timeFormat.format(calendar.getTime());

        String mMassageSenderRef = "Massages/" + mSenderId + "/" + mReceiverId;
        String mMassageReceiverRef = "Massages/" + mReceiverId + "/" + mSenderId;

        DatabaseReference mMassagesPushRef = mRootRef.child("Massages").child(mSenderId)
                .child(mReceiverId).push();
        String mMassagesPushId = mMassagesPushRef.getKey();

        HashMap<String, Object> MassagesTextBody = new HashMap<>();
        MassagesTextBody.put("massage", imageDownloadedUri.toString());
        MassagesTextBody.put("name", name);
        MassagesTextBody.put("type", mChecker);
        MassagesTextBody.put("from", mSenderId);
        MassagesTextBody.put("to", mReceiverId);
        MassagesTextBody.put("date", currentDateStatus);
        MassagesTextBody.put("time", currentTimeStatus);

        HashMap<String, Object> MassagesBodyDetails = new HashMap<>();
        MassagesBodyDetails.put(mMassageSenderRef + "/" + mMassagesPushId, MassagesTextBody);
        MassagesBodyDetails.put(mMassageReceiverRef + "/" + mMassagesPushId, MassagesTextBody);

        mRootRef.updateChildren(MassagesBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    loadingBar.dismiss();
                    Toast.makeText(InboxChatActivity.this, "Image Uploaded Successfully..!", Toast.LENGTH_SHORT).show();
                }else {
                    loadingBar.dismiss();
                    Toast.makeText(InboxChatActivity.this, "Error, cannot upload image..!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void displayLastSeenToolbar() {
        mRootRef.child("Users").child(mReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("userStatus").hasChild("status")) {

                    String status = dataSnapshot.child("userStatus").child("status").getValue().toString();
                    String date = dataSnapshot.child("userStatus").child("date").getValue().toString();
                    String time = dataSnapshot.child("userStatus").child("time").getValue().toString();

                    if (status.equals("online")) {
                        toolbarContactLast.setText("Online Now");
                    } else if (status.equals("offline")) {
                        toolbarContactLast.setText("Last Seen: " + "\n" + date + " " + time);
                    }

                } else {
                    toolbarContactLast.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getMassagesText();
    }

    private void getMassagesText() {
        mMassagesRef.child(mSenderId).child(mReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    String massageText = dataSnapshot.child("massage").getValue().toString();
                    String massageType = dataSnapshot.child("type").getValue().toString();
                    String massageForm = dataSnapshot.child("from").getValue().toString();
                    String massageTo = dataSnapshot.child("to").getValue().toString();
                    String massageDate = dataSnapshot.child("date").getValue().toString();
                    String massageTime = dataSnapshot.child("time").getValue().toString();
                    massages.add(new MassageModel(massageForm, massageText, massageType, massageTo, massageDate, massageTime));
                    adapter.notifyDataSetChanged();
                    massagesRecycler.smoothScrollToPosition(massagesRecycler.getAdapter().getItemCount());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()) {
                    String massageText = dataSnapshot.child("massage").getValue().toString();
                    String massageType = dataSnapshot.child("type").getValue().toString();
                    String massageForm = dataSnapshot.child("from").getValue().toString();
                    String massageTo = dataSnapshot.child("to").getValue().toString();
                    String massageDate = dataSnapshot.child("date").getValue().toString();
                    String massageTime = dataSnapshot.child("time").getValue().toString();
                    massages.add(new MassageModel(massageForm, massageText, massageType, massageTo, massageDate, massageTime));
                    adapter.notifyDataSetChanged();
                    massagesRecycler.smoothScrollToPosition(massagesRecycler.getAdapter().getItemCount());
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

    private void sendMassageText() {
        sendTextBtn.setEnabled(false);
        String massageText = massageEt.getText().toString();

        if (TextUtils.isEmpty(massageText)) {
            Toast.makeText(this, "Error: add any comment..!", Toast.LENGTH_SHORT).show();
        } else {

            String currentDateStatus, currentTimeStatus;

            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM,yyyy");
            currentDateStatus = dateFormat.format(calendar.getTime());

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            currentTimeStatus = timeFormat.format(calendar.getTime());

            String mMassageSenderRef = "Massages/" + mSenderId + "/" + mReceiverId;
            String mMassageReceiverRef = "Massages/" + mReceiverId + "/" + mSenderId;

            DatabaseReference mMassagesPushRef = mRootRef.child("Massages").child(mSenderId)
                    .child(mReceiverId).push();
            String mMassagesPushId = mMassagesPushRef.getKey();

            HashMap<String, Object> MassagesTextBody = new HashMap<>();
            MassagesTextBody.put("massage", massageText);
            MassagesTextBody.put("name", " ");
            MassagesTextBody.put("type", "text");
            MassagesTextBody.put("from", mSenderId);
            MassagesTextBody.put("to", mReceiverId);
            MassagesTextBody.put("date", currentDateStatus);
            MassagesTextBody.put("time", currentTimeStatus);

            HashMap<String, Object> MassagesBodyDetails = new HashMap<>();
            MassagesBodyDetails.put(mMassageSenderRef + "/" + mMassagesPushId, MassagesTextBody);
            MassagesBodyDetails.put(mMassageReceiverRef + "/" + mMassagesPushId, MassagesTextBody);

            mRootRef.updateChildren(MassagesBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        massagesRecycler.smoothScrollToPosition(massagesRecycler.getAdapter().getItemCount());
                        sendTextBtn.setEnabled(true);
                        massageEt.setText(" ");
                        Toast.makeText(InboxChatActivity.this, "Massage Sended Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        sendTextBtn.setEnabled(true);
                        massageEt.setText(" ");
                        Toast.makeText(InboxChatActivity.this, "Faild, Try Again..!", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
    }

    private void customToolBar() {
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View myView = layoutInflater.inflate(R.layout.custom_toobar_inbox_chat, null);
        actionBar.setCustomView(myView);

        toolbarContactImage = myView.findViewById(R.id.toolbar_contact_image);
        toolbarContactName = myView.findViewById(R.id.toolbar_contact_name);
        toolbarContactLast = myView.findViewById(R.id.toolbar_contact_last);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            mReceiverId = intent.getStringExtra(ChatsFragment.mContactId);
            mReceiverName = intent.getStringExtra(ChatsFragment.mContactName);
            mReceiverImage = intent.getStringExtra(ChatsFragment.mContactImage);

            //Toast.makeText(this, "" + mContactId + "\n" + mContactName + "\n" + mContactImage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
