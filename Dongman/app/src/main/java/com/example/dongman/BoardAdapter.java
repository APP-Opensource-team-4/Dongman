package com.example.dongman;

import android.content.Intent;
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
        h.views.setText("조회수 " + p.viewCount);      // 🔸 조회수 표시

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), PostDetailActivity.class);
            i.putExtra("postId", p.postId);
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    /* ───────────── 뷰홀더 ───────────── */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, preview, time, views;

        ViewHolder(@NonNull View v) {
            super(v);
            title   = v.findViewById(R.id.tv_post_title);
            preview = v.findViewById(R.id.tv_post_preview);
            time    = v.findViewById(R.id.tv_post_time);
            views   = v.findViewById(R.id.tv_post_views);  // id는 xml과 동일
        }
    }
}
