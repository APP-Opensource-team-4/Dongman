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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    /* ── UI ─────────────────────────────────────────────── */
    private TextView tvName, tvLocation;
    private View  rowMine, rowRealMine, rowRecent, rowLike, rowAlarm, rowLogout, rowLogin;
    private LinearLayout navHome, navFriend, navChat, navProfile;

    // 필드 선언부
    private View btnEditProfile;

    /* ── Firebase ───────────────────────────────────────── */
    private FirebaseAuth mAuth;

    /* ── life-cycle ─────────────────────────────────────── */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        initUI();
        setupMenuTexts();
        setupUserInfo();
        setupMenuListeners();
        setupBottomNavigation();
    }

    @Override protected void onResume() {
        super.onResume();
        setupUserInfo();                       // 갱신
    }

    /* ── UI 초기화 ───────────────────────────────────────── */
    private void initUI() {
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        tvName      = findViewById(R.id.tv_name);
        tvLocation  = findViewById(R.id.tv_location);

        rowMine     = findViewById(R.id.row_mine);
        rowRealMine = findViewById(R.id.row_real_mine);
        rowRecent   = findViewById(R.id.row_recent);
        rowLike     = findViewById(R.id.row_like);
        rowAlarm    = findViewById(R.id.row_alarm);
        rowLogout   = findViewById(R.id.row_logout);
        rowLogin    = findViewById(R.id.row_login);

        navHome   = findViewById(R.id.nav_home);
        navFriend = findViewById(R.id.nav_friend);
        navChat   = findViewById(R.id.nav_chat);
        navProfile= findViewById(R.id.nav_profile);
    }

    /* ── 메뉴 라벨 ───────────────────────────────────────── */
    private void setupMenuTexts() {
        setRowTitle(rowMine,     "참여 중인 모임");
        setRowTitle(rowRealMine, "내 모임");
        setRowTitle(rowRecent,   "최근 본 모임");
        setRowTitle(rowLike,     "찜한 모임");
        setRowTitle(rowAlarm,    "알림 설정");
        setRowTitle(rowLogout,   "로그아웃");
        setRowTitle(rowLogin,    "로그아웃");
    }
    private void setRowTitle(View row, String txt) {
        if (row == null) return;
        TextView tv = row.findViewById(R.id.tv_row_title);
        if (tv != null) tv.setText(txt);
    }

    /* ── 사용자 정보 표시 ───────────────────────────────── */
    private void setupUserInfo() {
        FirebaseAuth     auth = FirebaseAuth.getInstance();
        FirebaseFirestore db  = FirebaseFirestore.getInstance();
        FirebaseUser      cur = auth.getCurrentUser();

        if (cur == null) {
            tvName.setText("Guest");
            rowLogin .setVisibility(View.VISIBLE);
            rowLogout.setVisibility(View.GONE);

            auth.signInAnonymously()
                    .addOnSuccessListener(r -> {
                        String uid = r.getUser().getUid();
                        db.collection("users").document(uid)
                                .get()
                                .addOnSuccessListener(d -> {
                                    if (!d.exists()) {
                                        HashMap<String,Object> map = new HashMap<>();
                                        map.put("name","익명"); map.put("email","");
                                        db.collection("users").document(uid).set(map);
                                    }
                                    setupUserInfo();      // 또는 rowLogin GONE / rowLogout VISIBLE 직접 설정
                                });
                    });
            return;
        }

        /* B. Firebase 프로필 우선 */
        String disp = getDisplayName(cur);
        tvName.setText(disp);
        rowLogin .setVisibility(View.GONE);
        rowLogout.setVisibility(View.VISIBLE);

        /* C. Firestore 이메일-닉네임 조회 */
        String email = cur.getEmail();
        if (email == null || email.isEmpty()) return;

        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(qs -> {
                    if (!qs.isEmpty()) {
                        String nick = qs.getDocuments().get(0).getString("name");
                        if (nick != null && !nick.isEmpty()) tvName.setText(nick);
                    } else {
                        Log.w(TAG,"users 문서 없음(email 매칭)");
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG,"닉네임 로드 실패",e));
    }
    private String getDisplayName(FirebaseUser u) {
        if (u.getDisplayName()!=null && !u.getDisplayName().isEmpty()) return u.getDisplayName();
        if (u.getEmail()!=null && !u.getEmail().isEmpty())             return u.getEmail().split("@")[0];
        return "사용자";
    }

    /* ── 상단 메뉴 클릭 ───────────────────────────────── */
    private void setupMenuListeners() {

        rowMine.setOnClickListener(v ->
                Toast.makeText(this,"참여 중인 모임(구현 예정)",Toast.LENGTH_SHORT).show());

        rowRealMine.setOnClickListener(v -> {
            if (!LoginHelper.isLoggedIn(this)) {
                showLoginDialog("내 모임을 확인하려면 로그인이 필요합니다.");
                return;
            }
            startActivity(new Intent(this, MyPostsActivity.class));
        });

        rowRecent.setOnClickListener(
                v -> startActivity(new Intent(this, RecentActivity.class)));

        rowLike.setOnClickListener(
                v -> Toast.makeText(this,"찜한 모임(구현 예정)",Toast.LENGTH_SHORT).show());

        rowAlarm.setOnClickListener(
                v -> startActivity(new Intent(this, NotificationSettingsActivity.class)));

        rowLogout.setOnClickListener(v -> showLogoutDialog());

        rowLogin.setOnClickListener(
                v -> startActivity(new Intent(this, MainActivity.class)));

        // initUI()
                btnEditProfile = findViewById(R.id.btn_edit_profile);

        // setupMenuListeners()
                btnEditProfile.setOnClickListener(v ->
                        startActivity(new Intent(this, EditProfileActivity.class)));

    }

    /* ── 하단 네비 ───────────────────────────────────── */
    private void setupBottomNavigation() {

        navHome.setOnClickListener(
                v -> { Intent i=new Intent(this,MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(i); });

        navFriend.setOnClickListener(
                v -> startActivity(new Intent(this, BoardActivity.class)));

        navChat.setOnClickListener(
                v -> startActivity(new Intent(this, ChatActivity.class)));

        navProfile.setOnClickListener(
                v -> Toast.makeText(this,"현재 프로필 화면입니다",Toast.LENGTH_SHORT).show());
    }

    /* ── 다이얼로그 모음 ───────────────────────────────── */
    private void showLoginDialog(String msg){
        new AlertDialog.Builder(this)
                .setTitle("로그인 필요").setMessage(msg)
                .setPositiveButton("로그인하기",
                        (d,w)->startActivity(new Intent(this,LoginActivity.class)))
                .setNegativeButton("취소",null).show();
    }
    private void showLogoutDialog(){
        new AlertDialog.Builder(this)
                .setTitle("로그아웃").setMessage("정말 로그아웃하시겠습니까?")
                .setPositiveButton("로그아웃",(d,w)->{
                    mAuth.signOut();
                    LoginHelper.setLoggedIn(this,false);
                    Toast.makeText(this,"로그아웃되었습니다",Toast.LENGTH_SHORT).show();
                    Intent i=new Intent(this,MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i); finish();
                })
                .setNegativeButton("취소",null).show();
    }
}
