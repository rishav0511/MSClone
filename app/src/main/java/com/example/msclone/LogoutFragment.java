package com.example.msclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.msclone.Models.User;
import com.example.msclone.databinding.FragmentLogoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LogoutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogoutFragment extends Fragment {

    public LogoutFragment() {
        // Required empty public constructor
    }

    public static LogoutFragment newInstance(String param1, String param2) {
        LogoutFragment fragment = new LogoutFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private FirebaseDatabase mFirebaseDatabase;
    User current;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentLogoutBinding binding = FragmentLogoutBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mFirebaseDatabase.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    if(user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                        current = user;
                        if(current.getProfileImage()!=null) {
                            Glide.with(getContext()).load(current.getProfileImage())
                                    .placeholder(R.drawable.avatar)
                                    .into(binding.ProfileImage);
                            binding.mNameEditText.setText(current.getName());
                            binding.mEmailEditText2.setText(current.getEmail());
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(),Login.class);
                startActivity(intent);
                Toast.makeText(getActivity(), current.getEmail() + " Signed out!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}