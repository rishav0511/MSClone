package com.example.msclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.msclone.databinding.ActivityDashboardBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class Dashboard extends AppCompatActivity {

    private EditText mSecretCode;
    private Button mJoinBtn;
    private Button mShareBtn;

    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FragmentTransaction homeTrans = getSupportFragmentManager().beginTransaction();
        homeTrans.replace(R.id.content,new HomeFragment());
        homeTrans.commit();

        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        FragmentTransaction homeTrans = getSupportFragmentManager().beginTransaction();
                        homeTrans.replace(R.id.content,new HomeFragment());
                        homeTrans.commit();
                        Toast.makeText(Dashboard.this,"Home Selected", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.chat:
                        FragmentTransaction chatTrans = getSupportFragmentManager().beginTransaction();
                        chatTrans.replace(R.id.content,new ChatFragment());
                        chatTrans.commit();
                        Toast.makeText(Dashboard.this,"Chat Selected", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.logout:
                        FragmentTransaction logoutTrans = getSupportFragmentManager().beginTransaction();
                        logoutTrans.replace(R.id.content,new LogoutFragment());
                        logoutTrans.commit();
                        Toast.makeText(Dashboard.this,"Logout Selected", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
//        mSecretCode = findViewById(R.id.mSecretCode);
//        mJoinBtn = findViewById(R.id.mJoinBtn);
//        mShareBtn = findViewById(R.id.mShareBtn);
//
//        URL serverUrl;
//
//        try {
//            serverUrl = new URL("https://meet.jit.si");
//            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
//                    .setServerURL(serverUrl)
//                    .setWelcomePageEnabled(false)
//                    .build();
//            JitsiMeet.setDefaultConferenceOptions(options);
//        } catch (MalformedURLException e){
//            e.printStackTrace();
//        }


//        mJoinBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                JitsiMeetConferenceOptions options
//                        = new JitsiMeetConferenceOptions.Builder()
//                        .setRoom(mSecretCode.getText().toString())
//                        .setWelcomePageEnabled(false)
//                        .build();
//                JitsiMeetActivity.launch(Dashboard.this,options);
//            }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(this,"search clicked",Toast.LENGTH_SHORT).show();;
                break;
            case R.id.groups:
                Toast.makeText(this,"groups clicked",Toast.LENGTH_SHORT).show();;
                break;
            case R.id.invite:
                Toast.makeText(this,"invite clicked",Toast.LENGTH_SHORT).show();;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}