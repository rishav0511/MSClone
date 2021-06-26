package com.example.msclone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class Dashboard extends AppCompatActivity {

    private EditText mSecretCode;
    private Button mJoinBtn;
    private Button mShareBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mSecretCode = findViewById(R.id.mSecretCode);
        mJoinBtn = findViewById(R.id.mJoinBtn);
        mShareBtn = findViewById(R.id.mShareBtn);

        URL serverUrl;

        try {
            serverUrl = new URL("https://meet.jit.si");
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverUrl)
                    .setWelcomePageEnabled(false)
                    .build();
            JitsiMeet.setDefaultConferenceOptions(options);
        } catch (MalformedURLException e){
            e.printStackTrace();
        }


        mJoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JitsiMeetConferenceOptions options
                        = new JitsiMeetConferenceOptions.Builder()
                        .setRoom(mSecretCode.getText().toString())
                        .setWelcomePageEnabled(false)
                        .build();
                JitsiMeetActivity.launch(Dashboard.this,options);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}