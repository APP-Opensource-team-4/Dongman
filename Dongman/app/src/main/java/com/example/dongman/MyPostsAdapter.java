package com.example.dongman;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.PostViewHolder> {

    private List<Post> postList;
    private OnPostClickListener onPostClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Post post);
    }

    public MyPostsAdapter(List<Post> postList, OnPostClickListener postClickListener, OnDeleteClickListener deleteClickListener) {
        this.postList = postList;
        this.onPostClickListener = postClickListener;
        this.onDeleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // 제목 설정
        holder.titleTextView.setText(post.getTitle());

        // 내용 설정 (최대 2줄)
        String content = post.getContent();
        if (content.length() > 50) {
            content = content.substring(0, 50) + "...";
        }
        holder.contentTextView.setText(content);

        // 메타 정보 설정 (장소, 시간, 멤버)
        String metaInfo = "";
        if (post.getLocation() != null && !post.getLocation().isEmpty()) {
            metaInfo += post.getLocation() + " • ";
        }
        if (post.getTime() != null && !post.getTime().isEmpty()) {
            metaInfo += post.getTime() + " • ";
        }
        metaInfo += "최대 " + post.getMaxParticipants() + "명";
        holder.metaTextView.setText(metaInfo);

        // 작성 시간 설정
        if (post.getTimestamp() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
            holder.dateTextView.setText(dateFormat.format(post.getTimestamp()));
        }

        // 썸네일 이미지 설정
        if (post.getFirstImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(post.getFirstImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_thumbnail)
                    .error(R.drawable.placeholder_thumbnail)
                    .into(holder.thumbnailImageView);
        } else {
            // 기본 이미지 설정
            holder.thumbnailImageView.setImageResource(R.drawable.placeholder_thumbnail);
        }

        // 게시물 클릭 리스너 (상세 보기)
        holder.itemView.setOnClickListener(v -> {
            if (onPostClickListener != null) {
                onPostClickListener.onPostClick(post);
            }
        });

        // 수정 버튼 클릭 리스너 (미구현 - 토스트만)
        holder.editButton.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "수정 기능은 준비 중입니다", Toast.LENGTH_SHORT).show();
        });

        // 삭제 버튼 클릭 리스너
        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;
        TextView contentTextView;
        TextView metaTextView;
        TextView dateTextView;
        Button editButton;
        Button deleteButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.img_thumbnail);
            titleTextView = itemView.findViewById(R.id.tv_title);
            contentTextView = itemView.findViewById(R.id.tv_content);
            metaTextView = itemView.findViewById(R.id.tv_meta);
            dateTextView = itemView.findViewById(R.id.tv_date);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}