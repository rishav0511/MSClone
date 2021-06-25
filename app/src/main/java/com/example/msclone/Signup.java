package com.example.msclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;

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

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName,email,password;
                userName = mNameEditText.getText().toString();
                email = mEmailEditText.getText().toString();
                password = mPasswordEditText.getText().toString();

                mFirebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Signup.this,"Account is Created", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Signup.this,task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}