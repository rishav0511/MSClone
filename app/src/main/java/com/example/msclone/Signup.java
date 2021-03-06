package com.example.msclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.msclone.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Signup extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFireStore;

    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mLoginBtn;
    private Button mCreateBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mNameEditText = findViewById(R.id.mUserNameEditText);
        mEmailEditText = findViewById(R.id.mEmailEditText);
        mPasswordEditText = findViewById(R.id.mPasswordEditText);
        mLoginBtn = findViewById(R.id.mLoginBtn);
        mCreateBtn = findViewById(R.id.mCreateBtn);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFireStore = FirebaseFirestore.getInstance();

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName,email,password;
                userName = mNameEditText.getText().toString();
                email = mEmailEditText.getText().toString();
                password = mPasswordEditText.getText().toString();

                //check if all filed are entered
                if(userName.isEmpty() || email.isEmpty() || password.isEmpty()){
                    Toast.makeText(Signup.this,"Enter All Fields", Toast.LENGTH_LONG).show();
                    return;
                }
                User user = new User();     //create new user
                user.setName(userName);
                user.setEmail(email);
                user.setPassword(password);

                mFirebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            mFirebaseFireStore.collection("users")
                                    .document().set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // if user successfully created go to set up profile pic
                                    Intent intent = new Intent(Signup.this,Profilepic.class);
                                    intent.putExtra("username",userName);
                                    intent.putExtra("email",email);
                                    intent.putExtra("password",password);
                                    String uid = mFirebaseAuth.getUid();
                                    intent.putExtra("uid",uid);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            Toast.makeText(Signup.this,task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Signup.this,Login.class));
            }
        });
    }
}