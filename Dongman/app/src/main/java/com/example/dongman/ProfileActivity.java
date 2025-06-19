// ProfileActivity.java (수정 후)
package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth; // FirebaseAuth 추가

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
            FirebaseAuth.getInstance().signOut(); // Firebase 로그아웃 호출
            // LoginHelper.setLoggedIn(this,false); // 이 줄은 이제 필요 없습니다.

            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show(); // 로그아웃 메시지 추가

            // 로그아웃 후 MainActivity로 돌아가면서 이전 Activity 스택 클리어
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // ProfileActivity 종료
        });
    }

    private void setRowTitle(int rowId, String txt){
        ((TextView) findViewById(rowId)
                .findViewById(R.id.tv_row_title)).setText(txt);
    }
}