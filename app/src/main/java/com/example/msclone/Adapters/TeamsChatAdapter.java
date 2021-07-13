package com.example.msclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.msclone.Models.Message;
import com.example.msclone.Models.User;
import com.example.msclone.R;
import com.example.msclone.databinding.GroupItemRecievedBinding;
import com.example.msclone.databinding.GroupItemSentBinding;
import com.example.msclone.databinding.ItemRecievedBinding;
import com.example.msclone.databinding.ItemSentBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TeamsChatAdapter extends RecyclerView.Adapter{

    private Context mContext;
    private ArrayList<Message>messages;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;


    public TeamsChatAdapter(Context context, ArrayList<Message>messages){
        this.mContext = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_SENT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.group_item_sent,parent,false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.group_item_recieved,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };
        ReactionsConfig config = new ReactionsConfigBuilder(mContext)
                .withReactions(reactions)
                .build();
        ReactionPopup popup = new ReactionPopup(mContext, config, (pos) -> {
            if(holder.getClass()==SentViewHolder.class){
                SentViewHolder viewHolder = (SentViewHolder)holder;
                if(pos!=-1)
                {
                    viewHolder.binding.feeling.setImageResource(reactions[pos]);
                    viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                }
            }else{
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                if(pos!=-1)
                {
                    viewHolder.binding.feeling.setImageResource(reactions[pos]);
                    viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                }
            }

            message.setFeeling(pos);
            FirebaseDatabase.getInstance().getReference()
                    .child("public")
                    .child(message.getMessageId()).setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });

        if(holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder)holder;
            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(mContext)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(viewHolder.binding.image);
            }
            FirebaseDatabase.getInstance()
                    .getReference().child("users")
                    .child(message.getSenderId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.name.setText("@" + user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            viewHolder.binding.message.setText(message.getMessage());

            if(message.getFeeling()>=0)
            {
//                message.setFeeling(reactions[(int)message.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
            
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(mContext)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(viewHolder.binding.image);
            }
            FirebaseDatabase.getInstance()
                    .getReference().child("users")
                    .child(message.getSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.name.setText("@" + user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            viewHolder.binding.message.setText(message.getMessage());

            if(message.getFeeling()>=0)
            {
//                message.setFeeling(reactions[(int)message.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder{

        GroupItemSentBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = GroupItemSentBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{

        GroupItemRecievedBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = GroupItemRecievedBinding.bind(itemView);
        }
    }
}
