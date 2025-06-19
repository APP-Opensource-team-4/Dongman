package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyChatListActivity extends AppCompatActivity {

    private static final String TAG = "MyChatList";

    private final List<ChatRoom> roomList = new ArrayList<>();
    private ChatRoomAdapter adapter;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        /* ── Toolbar ───────────────────────────────────── */
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        /* ── RecyclerView ──────────────────────────────── */
        loading = findViewById(R.id.loading_bar);

        RecyclerView rv = findViewById(R.id.rv_chat_rooms);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatRoomAdapter(roomList, this::openChat);
        rv.setAdapter(adapter);

        loadRooms();
    }

    /* ─────────────────────────────────────────────────── */

    private void loadRooms() {
        loading.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance()
                .collection("chats")
                .orderBy("lastTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    roomList.clear();
                    qs.forEach(d -> {
                        ChatRoom r = d.toObject(ChatRoom.class);
                        if (r != null) { r.setId(d.getId()); roomList.add(r); }
                    });
                    adapter.notifyDataSetChanged();
                    loading.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "채팅 목록 로드 실패", e);
                    Toast.makeText(this, "채팅 목록 불러오기 실패", Toast.LENGTH_SHORT).show();
                    loading.setVisibility(View.GONE);
                });
    }

    private void openChat(int pos) {
        ChatRoom room = roomList.get(pos);

        Intent i = new Intent(this, ChatActivity.class)
                .putExtra("chatRoomId", room.getId())
                .putExtra("postTitle",  room.getPostTitle());
        // otherUserId / Name 은 단일 채팅이 아니라면 굳이 전달하지 않아도 됩니다.
        startActivity(i);
    }

    /* ─────────── RecyclerView 어댑터 ─────────── */

    static class ChatRoomAdapter extends RecyclerView.Adapter<VH> {
        interface Click { void on(int pos); }

        private final List<ChatRoom> data;
        private final Click click;

        ChatRoomAdapter(List<ChatRoom> d, Click c) { data = d; click = c; }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
            View item = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_chat_room, p, false);
            return new VH(item);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            ChatRoom r = data.get(pos);
            h.title.setText(r.getPostTitle());
            h.last.setText(r.getLastMessage());

            if (r.getLastTime() != null) {
                String t = new SimpleDateFormat("MM.dd HH:mm", Locale.KOREA)
                        .format(r.getLastTime().toDate());
                h.time.setText(t);
            } else {
                h.time.setText("");
            }

            h.itemView.setOnClickListener(v -> click.on(pos));
        }

        @Override public int getItemCount() { return data.size(); }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, last, time;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tv_room_title);
            last  = v.findViewById(R.id.tv_room_last);
            time  = v.findViewById(R.id.tv_room_time);
        }
    }
}
