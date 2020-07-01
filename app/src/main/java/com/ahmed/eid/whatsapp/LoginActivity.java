package com.ahmed.eid.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login_email)
    EditText loginEmail;
    @BindView(R.id.login_password)
    EditText loginPassword;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    DatabaseReference mUserRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Login");
        loadingBar.setMessage("please, Waiting...");
        loadingBar.setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            goToMainActivity();
        }
    }

    private void goToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }

    private void goToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    @OnClick({R.id.forget_password, R.id.login_btn, R.id.new_user_label, R.id.login_phone_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.forget_password:
                break;
            case R.id.login_btn:
                login();
                break;
            case R.id.new_user_label:
                goToRegisterActivity();
                break;
            case R.id.login_phone_btn:
                goToPhoneRegisterActivity();
                break;
        }
    }

    private void goToPhoneRegisterActivity() {
        Intent phoneRegisterIntent = new Intent(LoginActivity.this, PhoneRegisterActivity.class);
        startActivity(phoneRegisterIntent);
        finish();
    }

    private void login() {
        String email = loginEmail.getText().toString();
        String password = loginPassword.getText().toString();
        if (validation()) {
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String mDeviceToken = FirebaseInstanceId.getInstance().getToken();
                                String mUserId = mAuth.getCurrentUser().getUid();

                                mUserRef.child(mUserId).child("device_token")
                                        .setValue(mDeviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            goToMainActivity();
                                            Toast.makeText(LoginActivity.this, "loggedIn successfully", Toast.LENGTH_LONG).show();
                                            loadingBar.dismiss();
                                        }
                                    }

                                });

                            } else {
                                String errorMassage = task.getException().toString();
                                Toast.makeText(LoginActivity.this, errorMassage, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private Boolean validation() {
        String email = loginEmail.getText().toString();
        String password = loginPassword.getText().toString();

        if (email.isEmpty()) {
            loginEmail.setError("Please, Enter Email..");
            return false;
        } else if (password.isEmpty()) {
            loginPassword.setError("please, Enter Password..");
            return false;
        }
        return true;
    }

}


