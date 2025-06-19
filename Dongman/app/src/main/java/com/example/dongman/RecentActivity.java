package com.example.dongman;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecentActivity extends AppCompatActivity {

    private List<RecentMeeting> recentList;
    private RecentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent);

        // 툴바 설정 및 뒤로가기 버튼 → ProfileActivity로 이동
        Toolbar toolbar = findViewById(R.id.recent_toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(RecentActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // 최근 본 모임 불러오기
        recentList = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("recent_posts", MODE_PRIVATE);
        Gson gson = new Gson();
        Map<String, ?> allEntries = prefs.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String json = entry.getValue().toString();
            Post post = gson.fromJson(json, Post.class);

            if (post != null) {
                recentList.add(new RecentMeeting(
                        post.getTitle(),
                        post.getContent(), // 또는 post.getTime() 등 다른 속성도 가능
                        post.getLocation() + " · 멤버 " + post.getCount(),
                        post.getFirstImageUrl() // 🔥 imageUrl 추가
                ));
            }
        }

        // 리사이클러뷰 설정
        RecyclerView recyclerView = findViewById(R.id.rv_recent_meetings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecentAdapter(this, recentList, position -> {
            recentList.remove(position);
            adapter.notifyItemRemoved(position);
        });
        recyclerView.setAdapter(adapter);
    }
}
