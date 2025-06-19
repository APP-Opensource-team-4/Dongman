package com.example.dongman;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {

    private final Context context;
    private final List<RecentMeeting> meetingList;
    private final OnItemRemoveClickListener removeClickListener;

    public interface OnItemRemoveClickListener {
        void onRemoveClick(int position);
    }

    public RecentAdapter(Context context, List<RecentMeeting> meetingList, OnItemRemoveClickListener removeClickListener) {
        this.context = context;
        this.meetingList = meetingList;
        this.removeClickListener = removeClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_meeting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentMeeting meeting = meetingList.get(position);

        holder.tvTitle.setText(meeting.title);
        holder.tvDesc.setText(meeting.description);
        holder.tvTags.setText(meeting.tags);

        // 이미지 URL이 있는 경우 Glide로 로딩, 없으면 기본 이미지 사용
        if (meeting.imageUrl != null && !meeting.imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(meeting.imageUrl)
                    .placeholder(R.drawable.camera_logo)
                    .error(R.drawable.camera_logo)
                    .into(holder.imgThumbnail);
        } else {
            holder.imgThumbnail.setImageResource(R.drawable.camera_logo);
        }

        holder.btnRemove.setOnClickListener(v -> {
            if (removeClickListener != null) {
                removeClickListener.onRemoveClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail, btnRemove;
        TextView tvTitle, tvDesc, tvTags;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            tvTitle = itemView.findViewById(R.id.tv_group_title);
            tvDesc = itemView.findViewById(R.id.tv_group_desc);
            tvTags = itemView.findViewById(R.id.tv_group_tags);
        }
    }
}
