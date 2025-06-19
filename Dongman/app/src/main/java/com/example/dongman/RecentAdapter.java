package com.example.dongman;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.VH> {

    public interface OnRemoveClick {
        void onRemove(int position);
    }

    private final Context ctx;
    private final List<Post> data;
    private final OnRemoveClick remover;

    public RecentAdapter(Context c, List<Post> d, OnRemoveClick r) {
        this.ctx = c; this.data = d; this.remover = r;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_recent_meeting, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Post p = data.get(pos);
        h.title.setText(p.getTitle());
        h.desc .setText(p.getContent());
        h.tags .setText(p.getLocation() + " · 멤버 " + p.getCount());

        if (p.getFirstImageUrl() != null && !p.getFirstImageUrl().isEmpty()) {
            Glide.with(ctx).load(p.getFirstImageUrl())
                    .placeholder(R.drawable.placeholder_thumbnail)
                    .into(h.img);
        } else {
            h.img.setImageResource(R.drawable.placeholder_thumbnail);
        }

        h.btnRemove.setOnClickListener(v -> remover.onRemove(pos));

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, DetailActivity.class);
            i.putExtra("postId", p.getId());
            ctx.startActivity(i);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img, btnRemove;
        TextView  title, desc, tags;
        VH(@NonNull View v){
            super(v);
            img  = v.findViewById(R.id.img_thumbnail);
            btnRemove = v.findViewById(R.id.btn_remove);
            title= v.findViewById(R.id.tv_group_title);
            desc = v.findViewById(R.id.tv_group_desc);
            tags = v.findViewById(R.id.tv_group_tags);
        }
    }
}
