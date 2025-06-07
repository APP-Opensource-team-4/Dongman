package com.example.dongman;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder> {

    private final List<BoardPost> posts;

    public BoardAdapter(List<BoardPost> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_board_post, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        BoardPost p = posts.get(pos);
        h.title.setText(p.title);
        h.preview.setText(p.preview);
        h.time.setText(p.time);
        h.comments.setText("댓글 " + p.commentCount);
        // 닉네임 TextView는 XML에서 "익명"으로 고정
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    /* ───────────── 뷰홀더 ───────────── */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, preview, time, comments;

        ViewHolder(@NonNull View v) {
            super(v);
            title    = v.findViewById(R.id.tv_post_title);
            preview  = v.findViewById(R.id.tv_post_preview);
            time     = v.findViewById(R.id.tv_post_time);
            comments = v.findViewById(R.id.tv_post_comments);
        }
    }
}
