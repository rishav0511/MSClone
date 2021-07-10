package com.example.msclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.msclone.databinding.ActivityDashboardBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Dashboard extends AppCompatActivity {

    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //home fragment
        FragmentTransaction homeTrans = getSupportFragmentManager().beginTransaction();
        homeTrans.replace(R.id.content,new HomeFragment());
        homeTrans.commit();

        //Fragments for different activities
        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        FragmentTransaction homeTrans = getSupportFragmentManager().beginTransaction();
                        homeTrans.replace(R.id.content,new HomeFragment());
                        homeTrans.commit();
                        break;
                    case R.id.chat:
                        FragmentTransaction chatTrans = getSupportFragmentManager().beginTransaction();
                        chatTrans.replace(R.id.content,new ChatFragment());
                        chatTrans.commit();
                        break;
                    case R.id.common:
                        FragmentTransaction commonTrans = getSupportFragmentManager().beginTransaction();
                        commonTrans.replace(R.id.content,new CommonFragment());
                        commonTrans.commit();
                        break;
                    case R.id.logout:
                        FragmentTransaction logoutTrans = getSupportFragmentManager().beginTransaction();
                        logoutTrans.replace(R.id.content,new LogoutFragment());
                        logoutTrans.commit();
                        break;
                }
                return true;
            }
        });
    }


    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}