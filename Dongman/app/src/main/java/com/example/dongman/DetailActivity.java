package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        });

        Post p = (Post) getIntent().getSerializableExtra("post");
        if (p == null) finish();   // 안전장치

        ((ImageView) findViewById(R.id.img_cover)).setImageResource(p.imageRes);
        ((TextView)  findViewById(R.id.tv_title)).setText(p.title);
        ((TextView)  findViewById(R.id.tv_manager)).setText("모임장 : 동순이");
        ((TextView)  findViewById(R.id.tv_views)).setText("조회수 : 12");

        ((TextView)  findViewById(R.id.tv_body)).setText(
                "안녕하세요!\n오창호수공원에서 같이 밤에 달리실 분 구해요.\n"
                        + "저녁 드시고 천천히 나오시면 될 것 같아요.\n"
                        + "2명만 더 구하고 모집 종료하겠습니다!\n"
                        + "편하게 채팅 주세요");
    }
}
