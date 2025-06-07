package com.example.dongman;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView 어댑터 ‑ NEW 배지 기능 제거(레이아웃에 없으므로 오류 방지)
 */
public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.VH> {

    private final List<Post> items;
    private final View.OnClickListener itemClick;

    public MeetingAdapter(List<Post> items, View.OnClickListener click) {
        this.items = items;
        this.itemClick = click;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meeting, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Post p = items.get(pos);
        h.title.setText(p.title);
        h.content.setText(p.meta);
        h.meta.setText(p.location);
        h.thumb.setImageResource(p.imageRes);

        h.itemView.setTag(p);
        h.itemView.setOnClickListener(itemClick);
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView thumb; TextView title, content, meta;
        VH(View v) {
            super(v);
            thumb   = v.findViewById(R.id.item_thumbnail);
            title   = v.findViewById(R.id.item_title);
            content = v.findViewById(R.id.item_content);
            meta    = v.findViewById(R.id.item_meta);
        }
    }
}
