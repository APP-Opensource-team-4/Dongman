package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
<<<<<<< HEAD
=======
import android.util.Log;
import android.view.LayoutInflater;
>>>>>>> f5392973d0472f22d3859c9f0c813aaf98f433c2
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
<<<<<<< HEAD
import java.util.Random;
=======
import java.util.Map;
>>>>>>> f5392973d0472f22d3859c9f0c813aaf98f433c2

public class MainActivity extends AppCompatActivity {

    /* 데이터 */
    private final List<Post> meetingPosts = new ArrayList<>();
    private MeetingAdapter adapter;

    /* 필터 / 탭 버튼 */
    private TextView btnLatest, btnPopular, btnViews, btnNearby;
    private TextView menuNew, menuRecommend;
    private TextView currentFilter, currentTab;

    /* 글쓰기 결과 수신 */
    private final ActivityResultLauncher<Intent> writeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Post newPost = (Post) result.getData().getSerializableExtra("post");
                    if (newPost != null) addPost(newPost);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        attachListeners();

        seedMeetingData();
        setupRecycler();
        setupBottomNavigation();

        // 글쓰기 FAB
        ExtendedFloatingActionButton fab = findViewById(R.id.btn_write);
        fab.setOnClickListener(v -> writeLauncher.launch(new Intent(this, PostWriteActivity.class)));
    }

    /* ---------------------------- RecyclerView ---------------------------- */
    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rv_meetings);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MeetingAdapter(meetingPosts, v -> {
            Post p = (Post) v.getTag();
            startActivity(new Intent(this, DetailActivity.class).putExtra("post", p));
        });
        rv.setAdapter(adapter);
    }

    private void addPost(@NonNull Post p) {
        meetingPosts.add(0, p);
        adapter.notifyItemInserted(0);
        ((RecyclerView) findViewById(R.id.rv_meetings)).scrollToPosition(0);
    }

    /* ---------------------------- 버튼 바인딩 ---------------------------- */
    private void bindViews() {
        btnLatest  = findViewById(R.id.btn_latest);
        btnPopular = findViewById(R.id.btn_popular);
        btnViews   = findViewById(R.id.btn_views);
        btnNearby  = findViewById(R.id.btn_nearby);

        menuNew       = findViewById(R.id.menu_new);
        menuRecommend = findViewById(R.id.menu_recommend);

        currentFilter = btnLatest;
        currentTab    = menuRecommend;
    }

    private void attachListeners() {
        View.OnClickListener filterL = v -> {
            changeFilter((TextView) v);
            adapter.notifyDataSetChanged();
        };
        btnLatest.setOnClickListener(filterL);
        btnPopular.setOnClickListener(filterL);
        btnViews.setOnClickListener(filterL);
        btnNearby.setOnClickListener(filterL);

        View.OnClickListener tabL = v -> {
            changeTab((TextView) v);
            adapter.notifyDataSetChanged();
        };
        menuNew.setOnClickListener(tabL);
        menuRecommend.setOnClickListener(tabL);
    }

    private void changeFilter(TextView newFilter) {
        if (currentFilter != null) {
            currentFilter.setBackgroundColor(0xFFF5F5F5);
            currentFilter.setTextColor(0xFF666666);
        }
        newFilter.setBackgroundColor(0xFF000000);
        newFilter.setTextColor(0xFFFFFFFF);
        currentFilter = newFilter;
    }

    private void changeTab(TextView newTab) {
        if (currentTab != null) {
            currentTab.setTypeface(null, android.graphics.Typeface.NORMAL);
            currentTab.setTextColor(0xFF666666);
        }
        newTab.setTypeface(null, android.graphics.Typeface.BOLD);
        newTab.setTextColor(0xFF000000);
        currentTab = newTab;
    }

    /* ---------------------------- 하단 네비 ---------------------------- */
    private void setupBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {});
        findViewById(R.id.nav_friend).setOnClickListener(v ->
                startActivity(new Intent(this, BoardActivity.class)));
        findViewById(R.id.nav_chat).setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));
        findViewById(R.id.nav_profile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    /* ---------------------------- 더미 데이터 ---------------------------- */
    private void seedMeetingData() {
        String[] sports = {
                "자전거", "테니스/스쿼시", "볼링", "배드민턴", "스키/보드", "골프", "배구", "수영", "요가",
                "농구", "클라이밍", "축구", "러닝/마라톤", "야구", "주짓수", "검도", "헬스/크로스핏", "승마", "복싱", "족구", "다이어트"
        };
        Random r = new Random();
        for (int i = 0; i < 20; i++) {
            Post p = new Post();
            p.title    = "임시 제목 " + (i + 1);
            p.meta     = "임시 내용 " + (i + 1);
            p.location = sports[r.nextInt(sports.length)] + " • 청주시 • 멤버 " + (5 + i) + "명";
            p.imageRes = R.drawable.placeholder_thumbnail;
            meetingPosts.add(p);
        }
    }
}
