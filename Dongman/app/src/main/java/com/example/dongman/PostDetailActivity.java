package com.example.dongman;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvWriter, tvTime, tvViews, tvContent;
    private ProgressBar pb;
    private FirebaseFirestore db;
    private String postId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        /* ── UI 바인딩 ── */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle   = findViewById(R.id.tv_title);
        tvWriter  = findViewById(R.id.tv_writer);
        tvTime    = findViewById(R.id.tv_time);
        tvViews   = findViewById(R.id.tv_views);
        tvContent = findViewById(R.id.tv_content);
        pb        = findViewById(R.id.pb_loading);

        db     = FirebaseFirestore.getInstance();
        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "잘못된 접근입니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPost();                // ① 문서 읽기
        increaseViewCount();       // ② 조회수 +1
    }

    /** 글 내용 불러오기 */
    private void loadPost() {
        pb.setVisibility(View.VISIBLE);

        db.collection("boards").document(postId).get()
                .addOnSuccessListener(doc -> {
                    pb.setVisibility(View.GONE);
                    if (!doc.exists()) { finish(); return; }

                    tvTitle.setText(doc.getString("title"));
                    tvWriter.setText(doc.getString("writer"));

                    Timestamp ts = doc.getTimestamp("createdAt");
                    String timeStr = ts != null
                            ? new SimpleDateFormat("yyyy.MM.dd HH:mm",
                            Locale.getDefault()).format(ts.toDate())
                            : "";
                    tvTime.setText(timeStr);

                    Long viewsL = doc.getLong("viewCount");
                    int views   = viewsL != null ? viewsL.intValue() : 0;
                    tvViews.setText("조회수 " + views);

                    tvContent.setText(doc.getString("content"));
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "로드 실패: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /** 조회수 +1 */
    private void increaseViewCount() {
        db.collection("boards")
                .document(postId)
                .update("viewCount", FieldValue.increment(1));
    }
}
