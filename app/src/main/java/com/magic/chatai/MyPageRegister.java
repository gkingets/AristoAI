package com.magic.chatai;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyPageRegister extends AppCompatActivity {

    FirebaseAuth mAuth;

    TextInputEditText regMail;
    TextInputEditText regPassword;
    Button regBtn;
    TextView regToRegister;
    String uid;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypage_register);
        findView();
        createUser();







        regToRegister.setOnClickListener(view -> {
            startActivity(new Intent(MyPageRegister.this, MyPageLogin.class));
        });

    }

    private void findView() {
        regMail = findViewById(R.id.register_mail);
        regPassword = findViewById(R.id.register_password);
        regBtn = findViewById(R.id.register_button);
        regToRegister = findViewById(R.id.register_to_login);
        mAuth = FirebaseAuth.getInstance();
    }

    private void createUser() {
        regBtn.setOnClickListener(v -> {
            String mail = regMail.getText().toString();
            String password = regPassword.getText().toString();

            if (TextUtils.isEmpty(mail)) {
                regMail.setError("Email cannot be empty");
                regMail.requestFocus();
            } else if (TextUtils.isEmpty(password)) {
                regPassword.setError("Password cannot be empty");
                regPassword.requestFocus();
            } else {
                mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser(); // Register時にUIDを取得する

                            FuncFirebase funcFirebase = new FuncFirebase();
                            funcFirebase.createUser(user.getUid());

                            Toast.makeText(MyPageRegister.this, "User Registered successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MyPageRegister.this, MyPageLogin.class));
                        } else {
                            Toast.makeText(MyPageRegister.this, "Registration Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });
    }

}
