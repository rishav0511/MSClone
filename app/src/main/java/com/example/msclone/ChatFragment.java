package com.example.msclone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.msclone.Adapters.UsersAdapter;
import com.example.msclone.Models.User;
import com.example.msclone.databinding.FragmentChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    public ChatFragment() {
        // Required empty public constructor
    }


    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private FirebaseDatabase mFirebaseDatabase;
    ArrayList<User> users;
    UsersAdapter usersAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentChatBinding binding = FragmentChatBinding.inflate(inflater,container,false);
        View view = binding.getRoot();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(getActivity(),users);

        //attaching recycler view
        binding.recyclerView.setAdapter(usersAdapter);
        binding.recyclerView.showShimmerAdapter();

        //inflating up users
        mFirebaseDatabase.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    if(!user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                        users.add(user);
                }
                binding.recyclerView.hideShimmerAdapter();
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String presentUid = FirebaseAuth.getInstance().getUid();
        mFirebaseDatabase.getReference().child("presence")
                .child(presentUid)
                .setValue("Online");
    }

    @Override
    public void onPause() {
        super.onPause();
        String presentUid = FirebaseAuth.getInstance().getUid();
        mFirebaseDatabase.getReference().child("presence")
                .child(presentUid)
                .setValue("Offline");
    }

}