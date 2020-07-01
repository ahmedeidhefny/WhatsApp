package com.ahmed.eid.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PhoneRegisterActivity extends AppCompatActivity {

    EditText inputPhoneNumber, inputVerificationCode;
    Button verifyBtn, sendVerificationBtn;

    String mPhoneNumber, mVerificationId, mVerificationCode;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    PhoneAuthProvider.ForceResendingToken mResendToken;

    FirebaseAuth mAuth;
    DatabaseReference mRootRef;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_register);
        initializeUIVar();
        handleCallbacksObject();

        sendVerificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });


        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPhoneNumber.setVisibility(View.INVISIBLE);
                sendVerificationBtn.setVisibility(View.INVISIBLE);

                mVerificationCode = inputVerificationCode.getText().toString();
                if (TextUtils.isEmpty(mVerificationCode)) {
                    Toast.makeText(PhoneRegisterActivity.this, "Please, Write Verification Code...", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Verification Code....");
                    loadingBar.setMessage("Please Waiting, while we are authenticating with Verification Code ...!");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mVerificationCode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });
    }

    private void initializeUIVar() {
        inputPhoneNumber = findViewById(R.id.input_phone_number);
        inputVerificationCode = findViewById(R.id.input_verification_code);
        verifyBtn = findViewById(R.id.verify_btn);
        sendVerificationBtn = findViewById(R.id.send_verification_code_btn);
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        loadingBar = new ProgressDialog(this);
    }

    private void sendVerificationCode() {
        mPhoneNumber = inputPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(mPhoneNumber)) {
            Toast.makeText(PhoneRegisterActivity.this, "Please, Write Phone Number...", Toast.LENGTH_SHORT).show();
            return;
        } else {
            loadingBar.setTitle("Phone Verification....");
            loadingBar.setMessage("Please Waiting, while we are authenticating with phone number...!");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    mPhoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    PhoneRegisterActivity.this,
                    mCallbacks
            );
        }
    }

    private void handleCallbacksObject() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.cancel();

                Toast.makeText(PhoneRegisterActivity.this, "Invalid Phone Number," + "\n" + "please, Write correct phone number with country code.. ", Toast.LENGTH_SHORT).show();
                inputPhoneNumber.setVisibility(View.VISIBLE);
                sendVerificationBtn.setVisibility(View.VISIBLE);

                inputVerificationCode.setVisibility(View.INVISIBLE);
                verifyBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                loadingBar.cancel();

                mVerificationId = verificationId;
                mResendToken = token;

                inputPhoneNumber.setVisibility(View.INVISIBLE);
                sendVerificationBtn.setVisibility(View.INVISIBLE);

                inputVerificationCode.setVisibility(View.VISIBLE);
                verifyBtn.setVisibility(View.VISIBLE);
            }
        };
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String mDeviceToken = FirebaseInstanceId.getInstance().getToken();
                            String mUserId = mAuth.getCurrentUser().getUid();

                            HashMap<String, String> userDataMap = new HashMap<>();
                            userDataMap.put("id", mUserId);
                            userDataMap.put("device_token", mDeviceToken);

                            mRootRef.child("Users").child(mUserId).setValue(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        loadingBar.cancel();
                                        Toast.makeText(PhoneRegisterActivity.this, "Welcome to What's App..", Toast.LENGTH_SHORT).show();
                                        goToMainActivity();
                                    }
                                }
                            });

                        } else {
                            loadingBar.cancel();
                            String errorMassage = task.getException().toString();
                            Toast.makeText(PhoneRegisterActivity.this, "Error: " + errorMassage, Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }

    private void goToMainActivity() {
        Intent mainIntent = new Intent(PhoneRegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}
