// ProfileActivity.java
package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_profile);

        /* ───── 툴바 ───── */
        ((Toolbar) findViewById(R.id.toolbar))
                .setNavigationOnClickListener(v -> finish());

        /* ───── 행 제목 세팅 ───── */
        setRowTitle(R.id.row_mine   , "참여 중인 모임");
        setRowTitle(R.id.row_recent , "최근 본 모임");
        setRowTitle(R.id.row_like   , "찜한 모임");
        setRowTitle(R.id.row_alarm  , "알림 설정");
        setRowTitle(R.id.row_logout , "로그아웃");
        setRowTitle(R.id.row_login  , "로그인 (임시)");

        /* ───── 클릭 리스너 ───── */
        findViewById(R.id.row_login)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, LoginActivity.class)));

        findViewById(R.id.row_alarm)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, NotificationSettingsActivity.class)));

        findViewById(R.id.btn_edit_profile)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, EditProfileActivity.class)));

        findViewById(R.id.row_mine).setOnClickListener(v ->
                startActivity(new Intent(this, BoardActivity.class)));

        findViewById(R.id.row_recent).setOnClickListener(v -> {
            startActivity(new Intent(this, RecentActivity.class));
        });

        findViewById(R.id.row_like).setOnClickListener(v ->
                startActivity(new Intent(this, InterestActivity.class)));


        /* ───── 하단 네비 ───── */
        findViewById(R.id.nav_home   ).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)));

        findViewById(R.id.nav_friend ).setOnClickListener(v ->
                startActivity(new Intent(this, BoardActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)));

        findViewById(R.id.nav_chat   ).setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));

        /* ───── 로그인 상태에 따른 표시 ───── */
        boolean logged = LoginHelper.isLoggedIn(this);
        findViewById(R.id.row_login ).setVisibility(logged ? View.GONE : View.VISIBLE);
        findViewById(R.id.row_logout).setVisibility(logged ? View.VISIBLE : View.GONE);

        /* ───── 로그아웃 처리 ───── */
        findViewById(R.id.row_logout).setOnClickListener(v -> {
            LoginHelper.setLoggedIn(this,false);          // prefs → false
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
    }

    private void setRowTitle(int rowId, String txt){
        ((TextView) findViewById(rowId)
                .findViewById(R.id.tv_row_title)).setText(txt);
    }
}
