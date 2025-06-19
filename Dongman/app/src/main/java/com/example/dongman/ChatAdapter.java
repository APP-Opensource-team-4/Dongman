package com.example.dongman;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messageList;
    private FirebaseUser currentUser;

    public ChatAdapter(List<Message> l) {
        this.messageList = l;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public int getItemViewType(int pos) {
        Message message = messageList.get(pos);
        if (currentUser != null && currentUser.getUid().equals(message.getSenderId())) {
            return Message.Type.RIGHT.ordinal(); // 내가 보낸 메시지
        } else {
            return Message.Type.LEFT.ordinal(); // 상대방이 보낸 메시지
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        LayoutInflater inf = LayoutInflater.from(p.getContext());
        if (viewType == Message.Type.LEFT.ordinal()) {
            return new VHLeft(inf.inflate(R.layout.item_chat_left, p, false));
        }
        return new VHRight(inf.inflate(R.layout.item_chat_right, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = messageList.get(position); // messageList 사용

        if (holder instanceof VHLeft) {
            VHLeft h = (VHLeft) holder;
            h.tvSender.setText(m.getSender());
            h.tvMessage.setText(m.getText());
            if (m.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                h.tvTimestamp.setText(sdf.format(m.getTimestamp()));
            } else {
                h.tvTimestamp.setText("");
            }
            h.imgAvatar.setImageResource(R.drawable.myprofile_icon); // 아바타 아이콘 사용

        } else if (holder instanceof VHRight) {
            VHRight h = (VHRight) holder;
            h.tvMessage.setText(m.getText());
            if (m.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                h.tvTimestamp.setText(sdf.format(m.getTimestamp()));
            } else {
                h.tvTimestamp.setText("");
            }
        }
    }
    @Override public int getItemCount() { return messageList.size(); } // messageList 사용

    static class VHLeft extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvSender;
        TextView tvMessage;
        TextView tvTimestamp;

        VHLeft(View v){
            super(v);
            imgAvatar = v.findViewById(R.id.img_avatar);
            tvSender = v.findViewById(R.id.tv_sender);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTimestamp = v.findViewById(R.id.tv_timestamp_left);
        }
    }

    static class VHRight extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTimestamp;

        VHRight(View v){
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTimestamp = v.findViewById(R.id.tv_timestamp_right);
        }
    }
}