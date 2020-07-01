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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.register_email)
    EditText registerEmail;
    @BindView(R.id.register_password)
    EditText registerPassword;

    FirebaseAuth mAuth;
    DatabaseReference mRootRef;
    ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Create An Account");
        loadingBar.setMessage("please, Waiting...");
        loadingBar.setCanceledOnTouchOutside(true);
    }

    private void goToLoginActivity() {
        Intent LoginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(LoginIntent);
    }

    private void goToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }

    @OnClick({R.id.register_btn, R.id.already_have_account_label, R.id.register_phone_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.register_btn:
                createAnAccount();
                break;
            case R.id.already_have_account_label:
                goToLoginActivity();
                break;
            case R.id.register_phone_btn:
                goToPhoneRegisterActivity();
                break;
        }
    }

    private void goToPhoneRegisterActivity() {
        Intent phoneRegisterActivity = new Intent(RegisterActivity.this, PhoneRegisterActivity.class);
        startActivity(phoneRegisterActivity);
    }

    private void createAnAccount() {
        String email = registerEmail.getText().toString();
        String password = registerPassword.getText().toString();
        if (validation() == true) {
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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
                                            goToMainActivity();
                                            Toast.makeText(RegisterActivity.this, "an account created successfully..", Toast.LENGTH_LONG).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });

                            } else {
                                String errorMassage = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, errorMassage, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }


                    });
        }
    }

    private Boolean validation() {
        String email = registerEmail.getText().toString();
        String password = registerPassword.getText().toString();

        if (email.isEmpty()) {
            registerEmail.setError("Please, Enter Email..");
            return false;
        }
        if (password.isEmpty()) {
            registerPassword.setError("please, Enter Password..");
            return false;
        }
        return true;
    }
}
