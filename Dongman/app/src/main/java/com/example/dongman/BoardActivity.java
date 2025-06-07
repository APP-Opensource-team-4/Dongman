package com.example.dongman;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class BoardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BoardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);   // ← XML 파일

        // 1) Toolbar 객체로 꺼내기
        Toolbar toolbar = findViewById(R.id.toolbar_back);

        // 2) 필요하면 액션바로 등록
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 리스트 세팅
        recyclerView = findViewById(R.id.rv_board_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BoardAdapter(dummyPosts());
        recyclerView.setAdapter(adapter);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.white));

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /** 샘플용 더미 글 리스트 (닉네임은 모두 \"익명\") */
    private List<BoardPost> dummyPosts() {
        List<BoardPost> list = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            list.add(new BoardPost(
                    "게시글 제목 " + i,
                    "이곳에 간단한 미리보기 내용이 들어갑니다...",
                    "방금 전",            // 또는 포매팅된 시간
                    0                     // 댓글 수
            ));
        }
        return list;
    }
}
