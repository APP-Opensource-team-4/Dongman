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

    /** 샘플 데이터를 담아 둘 리스트 */
    private final List<Post> meetingPosts = new ArrayList<>();

    // ───────────────────────────────────────────────────────────── onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seedMeetingData();   // 1) 더미 데이터 준비
        fillMeetingList();   // 2) 화면에 데이터 바인딩
        setupBottomNavigation(); // 3) 하단 네비게이션 터치
    }

    // ───────────────────────────────────────────────────────────── 목록 채우기
    /** ScrollView 내부의 LinearLayout 을 얻어 모임 아이템을 동적으로 추가 */
    private void fillMeetingList() {
        // ScrollView → 첫 번째(유일) 자식 LinearLayout
        ScrollView scrollView = findViewById(R.id.meeting_container);
        LinearLayout container;

        // XML 에 이미 LinearLayout 이 존재하므로 child 0 이 LinearLayout 임을 보장
        if (scrollView.getChildCount() > 0 && scrollView.getChildAt(0) instanceof LinearLayout) {
            container = (LinearLayout) scrollView.getChildAt(0);
        } else {  // 혹시 모를 예외 상황 대응
            container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(container);
        }

        container.removeAllViews();                // XML 에 있던 더미 제거
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Post post : meetingPosts) {           // 데이터 → View 변환
            View item = createMeetingItem(inflater, post);
            container.addView(item);
        }
    }

    // ───────────────────────────────────────────────────────────── 아이템 생성
    /** 하나의 Post 객체를 받아 LinearLayout 형태의 아이템 View 반환 */
    private View createMeetingItem(LayoutInflater inflater, Post post) {
        // 수동 레이아웃 생성 (dp→px 환산값 사용)
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(0, 30, 0, 30); // 16dp ≒ 48px

        LinearLayout.LayoutParams lpMatchWrap = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        itemLayout.setLayoutParams(lpMatchWrap);

        // 썸네일
        ImageView thumbnail = new ImageView(this);
        LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(180, 180); // 60dp
        thumbParams.setMargins(0, 0, 48, 0); // 16dp
        thumbnail.setLayoutParams(thumbParams);
        thumbnail.setImageResource(post.imageRes);
        thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // 텍스트 컨테이너
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textContainer.setLayoutParams(textParams);

        // 제목
        TextView tvTitle = new TextView(this);
        tvTitle.setText(post.title);
        tvTitle.setTextSize(16);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(getColor(android.R.color.black));

        // 설명
        TextView tvMeta = new TextView(this);
        tvMeta.setText(post.meta);
        tvMeta.setTextSize(12);
        tvMeta.setTextColor(getColor(android.R.color.darker_gray));
        LinearLayout.LayoutParams metaParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        metaParams.setMargins(0, 12, 0, 0); // 4dp
        tvMeta.setLayoutParams(metaParams);

        // 위치/카테고리
        TextView tvLoc = new TextView(this);
        tvLoc.setText(post.location);
        tvLoc.setTextSize(12);
        tvLoc.setTextColor(getColor(android.R.color.tertiary_text_light));
        LinearLayout.LayoutParams locParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        locParams.setMargins(0, 12, 0, 0);
        tvLoc.setLayoutParams(locParams);

        // 조립
        textContainer.addView(tvTitle);
        textContainer.addView(tvMeta);
        textContainer.addView(tvLoc);
        itemLayout.addView(thumbnail);
        itemLayout.addView(textContainer);

        // 터치 → DetailActivity 전환
        itemLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("post", post);  // Post 는 Serializable
            startActivity(intent);
        });

        return itemLayout;
    }

    // ───────────────────────────────────────────────────────────── 네비게이션
    private void setupBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {}); // 현재 화면

        findViewById(R.id.nav_friend).setOnClickListener(v ->
                startActivity(new Intent(this, BoardActivity.class)));

        findViewById(R.id.nav_chat).setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));

        findViewById(R.id.nav_profile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    // ───────────────────────────────────────────────────────────── 더미 데이터
    private void seedMeetingData() {
        // 원하는 수만큼 루프를 늘려 보세요 (예: 12개)
        String[] categories = {
                "운동/스포츠", "업무/직무", "운동/스포츠", "아웃도어/여행",
                "운동/스포츠", "아웃도어/여행", "음식/미식", "봉사/나눔",
                "음악/공연", "IT/코딩", "게임/오락", "인문/독서"
        };

        for (int i = 0; i < categories.length; i++) {
            Post p = new Post();
            p.title    = "임시 제목 " + (i + 1);
            p.meta     = "임시 내용 " + (i + 1);
            p.location = categories[i] + " • 청주시 • 멤버 " + (5 + i) + "명";
            p.imageRes = R.drawable.placeholder_thumbnail;
            meetingPosts.add(p);
        }
    }

}
