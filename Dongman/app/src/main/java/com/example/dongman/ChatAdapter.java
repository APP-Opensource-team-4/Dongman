package com.example.dongman;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messageList.get(position);

        // 메시지 내용 설정
        holder.tvMessageContent.setText(message.getContent());

        // 시간 포맷팅
        String timeString = formatTime(message.getTimestamp());

        if (message.getType() == Message.Type.RIGHT) {
            // 내 메시지 (오른쪽)
            setupMyMessage(holder, timeString);
        } else {
            // 상대방 메시지 (왼쪽)
            setupOtherMessage(holder, message, timeString);
        }
    }

    private void setupMyMessage(ChatViewHolder holder, String timeString) {
        // 메시지 정렬을 오른쪽으로
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                holder.layoutMessageContent.getLayoutParams();
        params.gravity = Gravity.END;
        holder.layoutMessageContent.setLayoutParams(params);

        // 내 메시지 스타일 - 더 안전한 색상 사용
        holder.cardMessage.setCardBackgroundColor(0xFF007AFF); // 파란색
        holder.tvMessageContent.setTextColor(0xFFFFFFFF); // 흰색

        // 시간 표시 (메시지 왼쪽)
        if (holder.tvTimeRight != null) {
            holder.tvTimeRight.setText(timeString);
            holder.tvTimeRight.setVisibility(View.VISIBLE);
        }
        if (holder.tvTimeLeft != null) {
            holder.tvTimeLeft.setVisibility(View.GONE);
        }

        // 프로필 이미지 숨기기
        if (holder.imgProfileOther != null) {
            holder.imgProfileOther.setVisibility(View.GONE);
        }
        if (holder.imgProfileMine != null) {
            holder.imgProfileMine.setVisibility(View.GONE);
        }

        // 이름 숨기기
        if (holder.tvSenderName != null) {
            holder.tvSenderName.setVisibility(View.GONE);
        }
    }

    private void setupOtherMessage(ChatViewHolder holder, Message message, String timeString) {
        // 메시지 정렬을 왼쪽으로
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                holder.layoutMessageContent.getLayoutParams();
        params.gravity = Gravity.START;
        holder.layoutMessageContent.setLayoutParams(params);

        // 상대방 메시지 스타일
        holder.cardMessage.setCardBackgroundColor(0xFFFFFFFF); // 흰색
        holder.tvMessageContent.setTextColor(0xFF000000); // 검은색

        // 시간 표시 (메시지 오른쪽)
        if (holder.tvTimeLeft != null) {
            holder.tvTimeLeft.setText(timeString);
            holder.tvTimeLeft.setVisibility(View.VISIBLE);
        }
        if (holder.tvTimeRight != null) {
            holder.tvTimeRight.setVisibility(View.GONE);
        }

        // 프로필 이미지 표시
        if (holder.imgProfileOther != null) {
            holder.imgProfileOther.setVisibility(View.VISIBLE);
        }
        if (holder.imgProfileMine != null) {
            holder.imgProfileMine.setVisibility(View.GONE);
        }

        // 발신자 이름 표시
        if (holder.tvSenderName != null) {
            holder.tvSenderName.setText(message.getSenderName());
            holder.tvSenderName.setVisibility(View.VISIBLE);
        }
    }

    private String formatTime(Date timestamp) {
        if (timestamp == null) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.KOREA);
        return sdf.format(timestamp);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutMessageContainer;
        LinearLayout layoutMessageContent;
        CardView cardMessage;
        TextView tvMessageContent;
        TextView tvSenderName;
        TextView tvTimeLeft;
        TextView tvTimeRight;
        ImageView imgProfileOther;
        ImageView imgProfileMine;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            // 안전한 findViewById - null 체크 없이
            layoutMessageContainer = itemView.findViewById(R.id.layout_message_container);
            layoutMessageContent = itemView.findViewById(R.id.layout_message_content);
            cardMessage = itemView.findViewById(R.id.card_message);
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            tvTimeLeft = itemView.findViewById(R.id.tv_time_left);
            tvTimeRight = itemView.findViewById(R.id.tv_time_right);
            imgProfileOther = itemView.findViewById(R.id.img_profile_other);
            imgProfileMine = itemView.findViewById(R.id.img_profile_mine);
        }
    }
}