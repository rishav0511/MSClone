package com.example.msclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.msclone.Adapters.TeamsChatAdapter;
import com.example.msclone.Models.Message;
import com.example.msclone.databinding.FragmentCommonBinding;
import com.example.msclone.databinding.FragmentLogoutBinding;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CommonFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommonFragment extends Fragment {


    public CommonFragment() {
        // Required empty public constructor
    }

    public static CommonFragment newInstance(String param1, String param2) {
        CommonFragment fragment = new CommonFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private TeamsChatAdapter adapter;
    private FragmentCommonBinding binding;
    ArrayList<Message> messages;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private ProgressDialog dialog;
    String senderUid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommonBinding.inflate(inflater,container,false);
        View view = binding.getRoot();

//        getActivity().getActionBar().setTitle("Common");

        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseDatabase =FirebaseDatabase.getInstance();
        senderUid = FirebaseAuth.getInstance().getUid();

        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Uploading Image..");
        dialog.setCancelable(false);

        messages = new ArrayList<>();
        adapter = new TeamsChatAdapter(getContext(),messages);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        //messages to adapter
        mFirebaseDatabase.getReference().child("public")
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

        //messages send
        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();
                Date date = new Date();
                Message message = new Message(messageTxt,senderUid,date.getTime());
                binding.messageBox.setText("");

                mFirebaseDatabase.getReference().child("public")
                        .push()
                        .setValue(message);
            }
        });

        //sending photos
        binding.attachement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,49);
            }
        });

        return view;
    }

    //photos for common group
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==49){
            if(data!=null) {
                if(data.getData()!=null) {
                    Uri selectedImg = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = mFirebaseStorage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();;
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();

                                        String messageTxt = binding.messageBox.getText().toString();

                                        Date date = new Date();
                                        Message message = new Message(messageTxt, senderUid, date.getTime());
                                        message.setMessage("photo");
                                        message.setImageUrl(filePath);
                                        binding.messageBox.setText("");

                                        mFirebaseDatabase.getReference().child("public")
                                                .push()
                                                .setValue(message);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }
}