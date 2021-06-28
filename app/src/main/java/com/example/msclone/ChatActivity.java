package com.example.msclone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.msclone.Adapters.MessagesAdapter;
import com.example.msclone.Models.Message;
import com.example.msclone.databinding.ActivityChatBinding;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private MessagesAdapter adapter;
    ArrayList<Message> messages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this,messages);

        String name = getIntent().getStringExtra("name");
        String uid = getIntent().getStringExtra("uid");

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}