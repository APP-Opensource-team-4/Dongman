package com.example.dongman;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> list;
    public ChatAdapter(List<Message> l) { list = l; }

    @Override public int getItemViewType(int pos) { return list.get(pos).type.ordinal(); }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        LayoutInflater inf = LayoutInflater.from(p.getContext());
        if (viewType == Message.Type.LEFT.ordinal()) {
            return new VHLeft(inf.inflate(R.layout.item_chat_left, p, false));
        }
        return new VHRight(inf.inflate(R.layout.item_chat_right, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = list.get(position);

        if (holder instanceof VHLeft) {
            VHLeft h = (VHLeft) holder;                       // ★ 명시적 캐스팅
            ((TextView) h.itemView.findViewById(R.id.tv_sender)).setText(m.sender);
            ((TextView) h.itemView.findViewById(R.id.tv_message)).setText(m.text);

        } else if (holder instanceof VHRight) {
            VHRight h = (VHRight) holder;                      // ★ 명시적 캐스팅
            ((TextView) h.itemView.findViewById(R.id.tv_message)).setText(m.text);
        }
    }
    @Override public int getItemCount() { return list.size(); }

    static class VHLeft extends RecyclerView.ViewHolder { VHLeft(View v){ super(v);} }
    static class VHRight extends RecyclerView.ViewHolder { VHRight(View v){ super(v);} }
}
