package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DetailActivity extends AppCompatActivity {

    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 툴바 뒤로가기
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Post 데이터 수신
        post = (Post) getIntent().getSerializableExtra("post");
        if (post == null) {
            finish();
            return;
        }

        // View 바인딩
        ImageView imgCover = findViewById(R.id.img_cover);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvLocation = findViewById(R.id.tv_location);
        Button btnChat = findViewById(R.id.btn_chat);
        ImageButton btnMap = findViewById(R.id.btn_open_map);

        imgCover.setImageResource(post.imageRes);
        tvTitle.setText(post.title); // 제목 설정
        tvLocation.setText("장소 : " + post.location); // 전체 정보 표시

        // 채팅하기 버튼
        btnChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class))
        );

        // 지도 보기 버튼
        btnMap.setOnClickListener(v -> {
            // ✨ 지도에 전달할 위치 이름을 정제합니다.
            String rawLocation = post.location;
            String locationForMap = "";
            if (rawLocation != null && rawLocation.contains("•")) {
                String[] parts = rawLocation.split("•");
                if (parts.length > 1) {
                    // 예: "자전거 • 청주시 • 멤버 10명" -> "청주시" 추출
                    locationForMap = parts[1].trim();
                } else {
                    locationForMap = rawLocation.trim(); // 형식이 다를 경우 전체 사용
                }
            } else {
                locationForMap = rawLocation != null ? rawLocation.trim() : ""; // • 없을 경우 전체 사용
            }

            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("location_name", locationForMap); // ✨ 정제된 위치 이름 전달
            startActivity(intent);
        });
    }
}