package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // UI 요소
    private TextView tvName, tvLocation;
    private View rowMine, rowRealMine, rowRecent, rowLike, rowAlarm, rowLogout, rowLogin;

    // Bottom Navigation
    private LinearLayout navHome, navFriend, navChat, navProfile;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();

        // UI 초기화
        initializeUI();

        // 메뉴 텍스트 설정
        setupMenuTexts();

        // 사용자 정보 설정
        setupUserInfo();

        // 메뉴 클릭 리스너 설정
        setupMenuListeners();

        // 하단 네비게이션 설정
        setupBottomNavigation();
    }

    private void initializeUI() {
        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 사용자 정보
        tvName = findViewById(R.id.tv_name);
        tvLocation = findViewById(R.id.tv_location);

        // 메뉴 아이템들
        rowMine = findViewById(R.id.row_mine);
        rowRealMine = findViewById(R.id.row_real_mine);
        rowRecent = findViewById(R.id.row_recent);
        rowLike = findViewById(R.id.row_like);
        rowAlarm = findViewById(R.id.row_alarm);
        rowLogout = findViewById(R.id.row_logout);
        rowLogin = findViewById(R.id.row_login);

        // 하단 네비게이션
        navHome = findViewById(R.id.nav_home);
        navFriend = findViewById(R.id.nav_friend);
        navChat = findViewById(R.id.nav_chat);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void setupMenuTexts() {
        // 각 메뉴 항목의 텍스트 설정
        setMenuText(rowMine, "참여 중인 모임");
        setMenuText(rowRealMine, "내 모임");
        setMenuText(rowRecent, "최근 본 모임");
        setMenuText(rowLike, "찜한 모임");
        setMenuText(rowAlarm, "알림 설정");
        setMenuText(rowLogout, "로그아웃");
        setMenuText(rowLogin, "로그인");
    }

    private void setMenuText(View menuView, String text) {
        if (menuView != null) {
            TextView textView = menuView.findViewById(R.id.tv_row_title);
            if (textView != null) {
                textView.setText(text);
            }
        }
    }

    private void setupUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // 로그인된 사용자 정보 표시
            String displayName = getUserDisplayName(currentUser);
            tvName.setText(displayName);

            // 로그인 버튼 숨기기
            rowLogin.setVisibility(View.GONE);
            rowLogout.setVisibility(View.VISIBLE);
        } else {
            // 로그인되지 않은 상태
            tvName.setText("게스트");
            rowLogin.setVisibility(View.VISIBLE);
            rowLogout.setVisibility(View.GONE);
        }
    }

    private String getUserDisplayName(FirebaseUser user) {
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            return user.getDisplayName();
        } else if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            return user.getEmail().split("@")[0];
        } else {
            return "사용자";
        }
    }

    private void setupMenuListeners() {
        // 참여 중인 모임
        rowMine.setOnClickListener(v -> {
            Toast.makeText(this, "참여 중인 모임 (구현 예정)", Toast.LENGTH_SHORT).show();
        });

        // 내 모임 (작성한 모임)
        rowRealMine.setOnClickListener(v -> {
            // LoginHelper로 먼저 체크 (PostWriteActivity와 동일)
            if (!LoginHelper.isLoggedIn(this)) {
                showLoginRequiredDialog("내 모임을 확인하려면 로그인이 필요합니다.");
                return;
            }

            Intent intent = new Intent(this, MyPostsActivity.class);
            startActivity(intent);
        });

        // 최근 본 모임
        rowRecent.setOnClickListener(v -> {
            Toast.makeText(this, "최근 본 모임 (구현 예정)", Toast.LENGTH_SHORT).show();
        });

        // 찜한 모임
        rowLike.setOnClickListener(v -> {
            Toast.makeText(this, "찜한 모임 (구현 예정)", Toast.LENGTH_SHORT).show();
        });

        // 알림 설정
        rowAlarm.setOnClickListener(v -> {
            Toast.makeText(this, "알림 설정 (구현 예정)", Toast.LENGTH_SHORT).show();
        });

        // 로그아웃
        rowLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });

        // 로그인 (임시)
        rowLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        navFriend.setOnClickListener(v -> {
            Intent intent = new Intent(this, BoardActivity.class);
            startActivity(intent);
        });

        navChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            // 현재 화면이므로 아무것도 하지 않음
            Toast.makeText(this, "현재 프로필 화면입니다", Toast.LENGTH_SHORT).show();
        });
    }

    private void showLoginRequiredDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("로그인 필요")
                .setMessage(message)
                .setPositiveButton("로그인하기", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃하시겠습니까?")
                .setPositiveButton("로그아웃", (dialog, which) -> {
                    // Firebase 로그아웃
                    mAuth.signOut();

                    // SharedPreferences 로그아웃 상태 업데이트
                    LoginHelper.setLoggedIn(this, false);

                    Toast.makeText(this, "로그아웃되었습니다", Toast.LENGTH_SHORT).show();

                    // 메인 화면으로 이동
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 화면이 다시 보일 때마다 사용자 정보 업데이트
        setupUserInfo();
    }
}