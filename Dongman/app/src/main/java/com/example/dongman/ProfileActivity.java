package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        setTitle(findViewById(R.id.row_mine),   "참여 중인 모임");
        setTitle(findViewById(R.id.row_recent), "최근 본 모임");
        setTitle(findViewById(R.id.row_like),   "찜한 모임");
        setTitle(findViewById(R.id.row_alarm),  "알림 설정");
        setTitle(findViewById(R.id.row_logout), "로그아웃");
        setTitle(findViewById(R.id.row_login),  "로그인 (임시)");

        findViewById(R.id.row_login).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        findViewById(R.id.row_alarm).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationSettingsActivity.class)));

        findViewById(R.id.btn_edit_profile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)));

        findViewById(R.id.nav_friend).setOnClickListener(v ->
                startActivity(new Intent(this, BoardActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)));

        findViewById(R.id.nav_chat).setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));
    }

    private void setTitle(View row, String text) {
        ((TextView) row.findViewById(R.id.tv_row_title)).setText(text);
    }
}
