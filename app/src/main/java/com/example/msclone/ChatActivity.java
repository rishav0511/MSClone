package com.example.msclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.msclone.Adapters.MessagesAdapter;
import com.example.msclone.Models.Message;
import com.example.msclone.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom,receiverRoom;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    String receiverUid;
    String senderUid;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image..");
        dialog.setCancelable(false);

        messages = new ArrayList<>();

        String name = getIntent().getStringExtra("name");
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        adapter = new MessagesAdapter(this,messages,senderRoom,receiverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseDatabase =FirebaseDatabase.getInstance();
        mFirebaseDatabase.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();
                Date date = new Date();
                Message message = new Message(messageTxt,senderUid,date.getTime());
                binding.messageBox.setText("");

                String randomKey = mFirebaseDatabase.getReference().push().getKey();

                HashMap<String,Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg",message.getMessage());
                lastMsgObj.put("lastMsgTime",date.getTime());

                mFirebaseDatabase.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                mFirebaseDatabase.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                mFirebaseDatabase.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFirebaseDatabase.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });

                        HashMap<String,Object> lastMsgObj = new HashMap<>();
                        lastMsgObj.put("lastMsg",message.getMessage());
                        lastMsgObj.put("lastMsgTime",date.getTime());

                        mFirebaseDatabase.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                        mFirebaseDatabase.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
                    }
                });
            }
        });

        binding.attachement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,25);
            }
        });
            getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setTitle(name);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==25){
            if(data!=null){
                if(data.getData()!=null){
                    Uri selectedImg = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = mFirebaseStorage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();
                                        String messageTxt = binding.messageBox.getText().toString();
                                        Date date = new Date();
                                        Message message = new Message(messageTxt,senderUid,date.getTime());
                                        message.setMessage("Photo");
                                        message.setImageUrl(filePath);
                                        binding.messageBox.setText("");

                                        String randomKey = mFirebaseDatabase.getReference().push().getKey();

                                        HashMap<String,Object> lastMsgObj = new HashMap<>();
                                        lastMsgObj.put("lastMsg",message.getMessage());
                                        lastMsgObj.put("lastMsgTime",date.getTime());

                                        mFirebaseDatabase.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                        mFirebaseDatabase.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                                        mFirebaseDatabase.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mFirebaseDatabase.getReference().child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(randomKey)
                                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });

                                                HashMap<String,Object> lastMsgObj = new HashMap<>();
                                                lastMsgObj.put("lastMsg",message.getMessage());
                                                lastMsgObj.put("lastMsgTime",date.getTime());

                                                mFirebaseDatabase.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                                mFirebaseDatabase.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
                                            }
                                        });

                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}