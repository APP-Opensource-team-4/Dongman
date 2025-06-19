package com.example.dongman;

import android.content.Intent;                       // ★ 추가
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton; // ★ 추가
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BoardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BoardAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        /* ── 툴바 설정 ───────────────────────────── */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.white));
        toolbar.setNavigationOnClickListener(v -> finish());

        /* ── 글쓰기 버튼 → WriteBoardActivity ─────── */
        ExtendedFloatingActionButton btnWrite =
                findViewById(R.id.btn_write_board);
        btnWrite.setOnClickListener(v ->
                startActivity(new Intent(this, WriteBoardActivity.class)));

        /* ── 게시글 목록 세팅 ─────────────────────── */
        recyclerView = findViewById(R.id.rv_board_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        loadPostsFromFirestore();
    }

    private void loadPostsFromFirestore() {
        db.collection("boards")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    List<BoardPost> posts = new ArrayList<>();
                    for (DocumentSnapshot doc : qs) {
                        String postId  = doc.getId();
                        String title   = doc.getString("title");
                        String preview = doc.getString("preview");

                        Timestamp ts   = doc.getTimestamp("createdAt");
                        String time    = ts != null
                                ? new SimpleDateFormat("yyyy.MM.dd HH:mm",
                                Locale.getDefault()).format(ts.toDate())
                                : "";

                        Long viewCntL  = doc.getLong("viewCount");         // 🔸
                        int  views     = viewCntL != null ? viewCntL.intValue() : 0;

                        posts.add(new BoardPost(postId, title, preview, time, views));
                    }
                    adapter = new BoardAdapter(posts);
                    recyclerView.setAdapter(adapter);
                });
    }

    @Override protected void onResume() {
        super.onResume();
        loadPostsFromFirestore();   // 글 작성 후 돌아오면 최신 목록 재호출
    }

}
