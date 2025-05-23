package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<Post> postsNew = new ArrayList<>();
    private final List<Post> postsBeginner = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seed();

        LayoutInflater inf = LayoutInflater.from(this);
        fill(findViewById(R.id.list_new),      postsNew,      inf);
        fill(findViewById(R.id.list_beginner), postsBeginner, inf);

        /* ───── 터치 영역별 행동 ───── */

        // (A) 메인 콘텐츠 영역(하단 네비 위) → LoginActivity
        ScrollView content = findViewById(R.id.scrollContent);
        content.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        // (B) 하단 네비게이션 각 아이콘
        findViewById(R.id.nav_profile)
                .setOnClickListener(v -> startActivity(
                        new Intent(this, ProfileActivity.class)));

        findViewById(R.id.nav_chat)
                .setOnClickListener(v -> startActivity(
                        new Intent(this, ChatActivity.class)));

        findViewById(R.id.nav_friend)
                .setOnClickListener(v -> { /* 친구 목록 화면 */ });

        findViewById(R.id.nav_home)
                .setOnClickListener(v -> { /* 홈: 현재 화면 */ });
    }

    private void fill(LinearLayout target, List<Post> data, LayoutInflater inf) {
        for (Post p : data) {
            View row = inf.inflate(R.layout.item_temp_row, target, false);
            ((TextView) row.findViewById(R.id.row_title)).setText(p.title);
            ((TextView) row.findViewById(R.id.row_meta)).setText(p.meta);
            ((TextView) row.findViewById(R.id.row_location)).setText(p.location);
            ((ImageView) row.findViewById(R.id.row_thumb)).setImageResource(p.imageRes);

            row.setOnClickListener(v -> {
                Intent i = new Intent(this, DetailActivity.class);
                i.putExtra("post", p);
                startActivity(i);
            });
            target.addView(row);
        }
    }

    private void seed() {
        for (int i = 0; i < 3; i++) {
            Post sample = new Post();
            sample.title    = "배드민턴 모임";
            sample.meta     = "모집 1/10   조회수 : 1";
            sample.location = "충북대학교";
            sample.imageRes = R.drawable.placeholder_thumbnail;
            postsNew.add(sample);
            postsBeginner.add(sample);
        }
    }
}
