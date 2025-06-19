package com.example.dongman;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private ImageView imgCover;
    private TextView tvTitle, tvMeta, tvLocation, tvContent;
    private Button btnJoin;
    private ImageButton btnFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // View 바인딩
        imgCover = findViewById(R.id.img_cover);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvMeta = findViewById(R.id.tv_detail_meta);
        tvLocation = findViewById(R.id.tv_detail_location); // ← layout에 추가되어야 함
        tvContent = findViewById(R.id.tv_detail_content);
        btnJoin = findViewById(R.id.btn_join);
        btnFavorite = findViewById(R.id.btn_favorite);

        // Intent로부터 postId를 받음
        String postId = getIntent().getStringExtra("postId");
        if (postId != null) {
            loadPostData(postId);
        } else {
            Toast.makeText(this, "게시물 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadPostData(String postId) {
        DocumentReference docRef = db.collection("posts").document(postId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Post post = documentSnapshot.toObject(Post.class);

                if (post != null) {
                    tvTitle.setText(post.getTitle());
                    tvMeta.setText(post.getTime() + " | 멤버 " + post.getCount() + "명");
                    tvLocation.setText(post.getLocation());
                    tvContent.setText(post.getContent());
                    getSupportActionBar().setTitle(post.getTitle()); // ✅ 툴바 제목 설정 추가
                    if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
                        Glide.with(this).load(post.getFirstImageUrl()).into(imgCover);
                    }
                }
            } else {
                Toast.makeText(this, "해당 게시물이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "게시물 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
