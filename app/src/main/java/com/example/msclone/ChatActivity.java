package com.example.msclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseDatabase =FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image..");
        dialog.setCancelable(false);

        messages = new ArrayList<>();

        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("image");
        String token = getIntent().getStringExtra("token");

        binding.name.setText(name);

        //Profile image
        Glide.with(ChatActivity.this)
                .load(profile)
                .placeholder(R.drawable.avatar)
                .into(binding.profileimage);
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //getting user's Uids
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        //for making rooms between 2 user
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        //status of user
        mFirebaseDatabase.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    if(!status.isEmpty()) {
                        if(status.equals("Offline")){
                            binding.status.setVisibility(View.GONE);
                        } else {
                            binding.status.setText(status);
                            binding.status.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Adapter
        adapter = new MessagesAdapter(this,messages,senderRoom,receiverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        //messages to adapter
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

        //send message
        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();
                Date date = new Date();
                Message message = new Message(messageTxt,senderUid,date.getTime());
                binding.messageBox.setText("");

                String randomKey = mFirebaseDatabase.getReference().push().getKey();

                //most recent message
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
                                sendNotification(name,message.getMessage(),token);
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

        //attaching photos
        binding.attachement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,25);
            }
        });

        //check if something has benn typed
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()>0)
                    binding.sendButton.setEnabled(true);
                else
                    binding.sendButton.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
            getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setTitle(name);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void sendNotification(String name,String message,String token){
        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title",name);
            data.put("body",message);

            JSONObject notificationData = new JSONObject();
            notificationData.put("notification",data);
            notificationData.put("to",token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
//                    Toast.makeText(ChatActivity.this,"success",Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ChatActivity.this,error.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String,String>map = new HashMap<>();
                    String key="Key=AAAAcd_YNrA:APA91bGBnSgtKFXuL0DPyW4IRH_24CwWjW8oxMQUdawQVqh16HekbrZZMDXVnRShjiE2h2jfFZGIWG13BGzc-NL5kFZrgulpXQY49PhW_5RpaiyT0IaL_-8gs_WMnxs7Dy-t6O1bBwfl";
                    map.put("Authorization",key);
                    map.put("Content-Type","application/json");
                    return map;
                }
            };
            queue.add(request);

        } catch (Exception ex){

        }


    }
    //for photos
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
    protected void onResume() {
        super.onResume();
        String presentUid = FirebaseAuth.getInstance().getUid();
        mFirebaseDatabase.getReference().child("presence")
                .child(presentUid)
                .setValue("Online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        String presentUid = FirebaseAuth.getInstance().getUid();
        mFirebaseDatabase.getReference().child("presence")
                .child(presentUid)
                .setValue("Offline");
    }

    @Override
    public void onPause() {
        super.onPause();
        String presentUid = FirebaseAuth.getInstance().getUid();
        mFirebaseDatabase.getReference().child("presence")
                .child(presentUid)
                .setValue("Offline");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}