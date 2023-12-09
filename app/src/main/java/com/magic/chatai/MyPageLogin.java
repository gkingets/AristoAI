package com.magic.chatai;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;

import com.google.android.gms.auth.api.identity.SignInClient;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MyPageLogin extends AppCompatActivity {

    FirebaseAuth mAuth;
    TextInputEditText loginMail;
    TextInputEditText loginPassword;
    Button loginBtn;
    TextView loginToRegister;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypage_login);
        mAuth = FirebaseAuth.getInstance();
        findView();






        loginUser();
        loginToRegister.setOnClickListener(view -> {
            startActivity(new Intent(MyPageLogin.this, MyPageRegister.class));
        });

    }


    private void findView() {
        loginMail = findViewById(R.id.login_mail);
        loginPassword = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_button);
        loginToRegister = findViewById(R.id.login_to_register);
    }

    private void loginUser() {
        loginBtn.setOnClickListener(v -> {
            String mail = loginMail.getText().toString();
            String password = loginPassword.getText().toString();

            if (TextUtils.isEmpty(mail)) {
                loginMail.setError("Email cannot be empty");
                loginMail.requestFocus();
            } else if (TextUtils.isEmpty(password)) {
                loginPassword.setError("Password cannot be empty");
                loginPassword.requestFocus();
            } else {
                mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MyPageLogin.this, "Sing in successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MyPageLogin.this, MainActivity.class));
                        } else {
                            Toast.makeText(MyPageLogin.this, "Sing in Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }


}
