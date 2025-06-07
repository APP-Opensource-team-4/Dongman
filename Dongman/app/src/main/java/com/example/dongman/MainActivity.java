package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    /* 리스트 데이터 */
    private final List<Post> meetingPosts = new ArrayList<>();
    private MeetingAdapter  adapter;

    /* 중간 필터 / 상단 탭 */
    private TextView btnLatest, btnPopular, btnViews, btnNearby;
    private TextView menuNew, menuRecommend;
    private TextView currentFilter, currentTab;

    /* 글쓰기 결과 */
    private final ActivityResultLauncher<Intent> writeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK && r.getData() != null) {
                    Post p = (Post) r.getData().getSerializableExtra("post");
                    if (p != null) addPost(p);
                }
            });

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        bindViews();
        attachListeners();
        seedMeetingData();
        setupRecycler();
        setupBottomNavigation();

        ExtendedFloatingActionButton fabWrite = findViewById(R.id.btn_write);
        fabWrite.setOnClickListener(v ->
                writeLauncher.launch(new Intent(this, PostWriteActivity.class)));
    }

    /* ───────── RecyclerView ───────── */
    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rv_meetings);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MeetingAdapter(meetingPosts,
                v -> startActivity(new Intent(this, DetailActivity.class)
                        .putExtra("post", (Post) v.getTag())));
        rv.setAdapter(adapter);
    }

    private void addPost(@NonNull Post p) {
        meetingPosts.add(0, p);
        adapter.notifyItemInserted(0);
        ((RecyclerView) findViewById(R.id.rv_meetings)).scrollToPosition(0);
    }

    /* ───────── 버튼 바인딩/리스너 ───────── */
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
        View.OnClickListener f = v -> {
            changeFilter((TextView) v);
            adapter.notifyDataSetChanged();
        };
        btnLatest.setOnClickListener(f);
        btnPopular.setOnClickListener(f);
        btnViews.setOnClickListener(f);
        btnNearby.setOnClickListener(f);

        View.OnClickListener t = v -> {
            changeTab((TextView) v);
            adapter.notifyDataSetChanged();
        };
        menuNew.setOnClickListener(t);
        menuRecommend.setOnClickListener(t);
    }

    private void changeFilter(TextView n) {
        currentFilter.setBackgroundColor(0xFFF5F5F5);
        currentFilter.setTextColor(0xFF666666);
        n.setBackgroundColor(0xFF000000);
        n.setTextColor(0xFFFFFFFF);
        currentFilter = n;
    }

    private void changeTab(TextView n) {
        currentTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        currentTab.setTextColor(0xFF666666);
        n.setTypeface(null, android.graphics.Typeface.BOLD);
        n.setTextColor(0xFF000000);
        currentTab = n;
    }

    /* ───────── 하단 네비 ───────── */
    private void setupBottomNavigation() {
        findViewById(R.id.nav_home   ).setOnClickListener(v -> {}); // 현재 화면
        findViewById(R.id.nav_friend ).setOnClickListener(v -> safeLaunch(BoardActivity.class));
        findViewById(R.id.nav_chat   ).setOnClickListener(v -> safeLaunch(ChatActivity.class));
        findViewById(R.id.nav_profile).setOnClickListener(v -> safeLaunch(ProfileActivity.class));
    }

    /** 로그인 필요 기능 실행용 공통 메서드 */
// ① 화면 아무 곳 터치 시 로그인 체크
    @Override public boolean dispatchTouchEvent(@NonNull android.view.MotionEvent e){
        if(!LoginHelper.isLoggedIn(this)&&e.getAction()==android.view.MotionEvent.ACTION_DOWN){
            new AlertDialog.Builder(this)
                    .setTitle("로그인이 필요합니다")
                    .setMessage("해당 기능은 로그인 후 이용할 수 있습니다.")
                    .setPositiveButton("로그인하기",
                            (d,w)->startActivity(new Intent(this,LoginActivity.class)))
                    .setNegativeButton("닫기",null)
                    .show();
            return true;
        }
        return super.dispatchTouchEvent(e);
    }

    // ② safeLaunch 다시 추가 (하단바·프로필 재사용용)
    private void safeLaunch(Class<?> c){
        if(LoginHelper.isLoggedIn(this)){
            startActivity(new Intent(this,c));
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("로그인이 필요합니다")
                    .setMessage("해당 기능은 로그인 후 이용할 수 있습니다.")
                    .setPositiveButton("로그인하기",
                            (d,w)->startActivity(new Intent(this,LoginActivity.class)))
                    .setNegativeButton("닫기",null)
                    .show();
        }
    }

    /* ───────── 더미 데이터 ───────── */
    private void seedMeetingData() {
        String[] sport = {"자전거","테니스/스쿼시","볼링","배드민턴","스키/보드","골프","배구",
                "수영","요가","농구","클라이밍","축구","러닝/마라톤","야구","주짓수","검도",
                "헬스/크로스핏","승마","복싱","족구","다이어트"};
        Random r = new Random();
        for (int i = 0; i < 20; i++) {
            Post p = new Post();
            p.title    = "임시 제목 " + (i + 1);
            p.meta     = "임시 내용 " + (i + 1);
            p.location = sport[r.nextInt(sport.length)] + " • 청주시 • 멤버 " + (5 + i) + "명";
            p.imageRes = R.drawable.placeholder_thumbnail;
            meetingPosts.add(p);
        }
    }
}
