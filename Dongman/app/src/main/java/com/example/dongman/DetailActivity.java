package com.example.dongman;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Toast를 사용하기 위해 추가
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide; // Make sure Glide is in your build.gradle

public class DetailActivity extends AppCompatActivity {

    private ImageView imgCover;
    private TextView tvTitle, tvMeta, tvLocation, tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail); // activity_detail.xml 레이아웃이 필요합니다.

        // UI 요소 초기화
        imgCover = findViewById(R.id.img_cover);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvMeta = findViewById(R.id.tv_detail_meta);
        tvLocation = findViewById(R.id.tv_detail_location);
        tvContent = findViewById(R.id.tv_detail_content);

        // 이전 Activity에서 전달된 Post 객체 가져오기
        // Serializable 객체를 안전하게 캐스팅
        Post post = (Post) getIntent().getSerializableExtra("post");

        if (post != null) {
            // TextView에 데이터 설정
            tvTitle.setText(post.getTitle());
            String metaText = post.getTime() + " | 멤버 " + post.getCount() + "명";
            tvMeta.setText(metaText);
            tvLocation.setText(post.getLocation());
            tvContent.setText(post.getContent());

            // 이미지 처리 (Glide 사용)
            if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
                String imageUrl = post.getImageUrls().get(0); // 첫 번째 이미지 URL 로드
                Glide.with(this)
                        .load(imageUrl)
                        // .placeholder(R.drawable.placeholder_image) // placeholder_image가 없다면 이 줄을 제거하거나 아래처럼 변경
                        .placeholder(R.drawable.camera_logo) // 임시로 camera_logo 사용
                        // .error(R.drawable.error_image) // error_image가 없다면 이 줄을 제거하거나 아래처럼 변경
                        .error(R.drawable.camera_logo) // 임시로 camera_logo 사용
                        .into(imgCover);
            } else {
                // 게시물에 이미지가 없을 경우 기본 이미지 설정 (default_post_image가 없다고 가정)
                // 이전에 default_post_image 오류가 났었으므로, 여기도 camera_logo로 대체
                imgCover.setImageResource(R.drawable.camera_logo);
            }
        } else {
            // Post 객체가 null인 경우 (데이터가 제대로 전달되지 않은 경우)
            Toast.makeText(this, "게시물 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 현재 Activity 종료
        }

        // 선택 사항: 툴바 뒤로가기 버튼 설정 예시 (activity_detail.xml에 Toolbar가 있다면)
        // Toolbar toolbar = findViewById(R.id.toolbar);
        // if (toolbar != null) {
        //     setSupportActionBar(toolbar);
        //     if (getSupportActionBar() != null) {
        //         getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 화살표 표시
        //         getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 숨김 (필요시)
        //     }
        //     toolbar.setNavigationOnClickListener(v -> finish()); // 뒤로가기 버튼 클릭 시 Activity 종료
        // }
    }
}