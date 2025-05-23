package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        setTitle(findViewById(R.id.row_mine),   "내 모임");
        setTitle(findViewById(R.id.row_recent), "최근 본 모임");
        setTitle(findViewById(R.id.row_like),   "찜한 모임");
        setTitle(findViewById(R.id.row_alarm),  "알림 설정");
        setTitle(findViewById(R.id.row_logout), "로그아웃");
        setTitle(findViewById(R.id.row_login),  "로그인 (임시)");
        findViewById(R.id.row_login).setOnClickListener(v ->          // 클릭 → 로그인 화면
                startActivity(new Intent(this, LoginActivity.class)));

        // 프로필 수정 → EditProfileActivity
        findViewById(R.id.btn_edit_profile)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, EditProfileActivity.class)));

        /* ───── 하단 네비게이션 리스너 ───── */
        // 홈
        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)));

        // 내 채팅
        findViewById(R.id.nav_chat).setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));

        // (선택) 친구 목록, 내 프로필 아이콘은 현재 화면이므로 동작 없이 둘 수 있음
    }

    private void setTitle(View row, String text) {
        ((TextView) row.findViewById(R.id.tv_row_title)).setText(text);
    }
}
