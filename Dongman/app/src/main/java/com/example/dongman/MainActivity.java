// MainActivity.java
package com.example.dongman;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private MeetingAdapter adapter;
    private final List<Post> postList = new ArrayList<>();
    private ProgressBar loadingBar;
    private ListenerRegistration firestoreListener;

    // Filter and Tab UI elements
    private TextView btnLatest, btnPopular, btnViews, btnNearby;
    private TextView menuNew, menuRecommend;
    private TextView currentFilter;
    private TextView currentTab;

    // Bottom Navigation
    private LinearLayout navHome, navFriend, navChat, navProfile;

    // Write post button
    private ExtendedFloatingActionButton btnWrite;

    /* Write result launcher */
    private final ActivityResultLauncher<Intent> writeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(this, "게시물이 성공적으로 작성되었습니다!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "PostWriteActivity completed successfully.");
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        // UI 초기화
        initializeUI();

        // 바인딩 및 리스너 설정
        bindViews();
        attachListeners();
        setupBottomNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListeningForPosts();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recycler_view);
        loadingBar = findViewById(R.id.loading_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 어댑터 초기화
        adapter = new MeetingAdapter(postList, v -> {
            Post clickedPost = (Post) v.getTag();
            if (clickedPost != null && clickedPost.getId() != null) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("postId", clickedPost.getId());
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "게시물 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        // 글쓰기 버튼
        btnWrite = findViewById(R.id.btn_write);
        btnWrite.setOnClickListener(v -> safeLaunch(PostWriteActivity.class));
    }

    private void startListeningForPosts() {
        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        loadingBar.setVisibility(View.VISIBLE);
        Query baseQuery = db.collection("posts");

        // 필터에 따른 정렬
        if (currentFilter == btnPopular) {
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING);
            Toast.makeText(this, "인기순 필터 (구현 예정)", Toast.LENGTH_SHORT).show();
        } else if (currentFilter == btnViews) {
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING);
            Toast.makeText(this, "조회순 필터 (구현 예정)", Toast.LENGTH_SHORT).show();
        } else if (currentFilter == btnNearby) {
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING);
            Toast.makeText(this, "가까운순 필터 (구현 예정)", Toast.LENGTH_SHORT).show();
        } else {
            // 기본값: 최신순
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING);
        }

        firestoreListener = baseQuery.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                Toast.makeText(this, "게시물 로딩 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.setVisibility(View.GONE);
                return;
            }

            if (snapshots != null) {
                postList.clear();
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    Post post = document.toObject(Post.class);
                    if (post != null) {
                        post.setId(document.getId());
                        if (post.getImageUrls() == null) {
                            post.setImageUrls(new ArrayList<>());
                        }
                        postList.add(post);
                    }
                }
                adapter.notifyDataSetChanged();
                loadingBar.setVisibility(View.GONE);

                if (!postList.isEmpty()) {
                    recyclerView.scrollToPosition(0);
                }
            }
        });
    }

    /* ───────── UI Binding and Listeners ───────── */
    private void bindViews() {
        btnLatest = findViewById(R.id.btn_latest);
        btnPopular = findViewById(R.id.btn_popular);
        btnViews = findViewById(R.id.btn_views);
        btnNearby = findViewById(R.id.btn_nearby);

        menuNew = findViewById(R.id.menu_new);
        menuRecommend = findViewById(R.id.menu_recommend);

        // 초기 선택 상태 설정
        currentFilter = btnLatest;
        currentTab = menuRecommend;
    }

    private void attachListeners() {
        View.OnClickListener filterListener = v -> {
            changeFilter((TextView) v);
            startListeningForPosts();
        };
        btnLatest.setOnClickListener(filterListener);
        btnPopular.setOnClickListener(filterListener);
        btnViews.setOnClickListener(filterListener);
        btnNearby.setOnClickListener(filterListener);

        View.OnClickListener tabListener = v -> {
            changeTab((TextView) v);
            startListeningForPosts();
        };
        menuNew.setOnClickListener(tabListener);
        menuRecommend.setOnClickListener(tabListener);

        // 초기 스타일 적용
        changeFilter(currentFilter);
        changeTab(currentTab);
    }

    private void changeFilter(TextView selected) {
        if (currentFilter != null) {
            currentFilter.setBackgroundColor(0xFFF5F5F5);
            currentFilter.setTextColor(0xFF666666);
        }
        selected.setBackgroundColor(0xFF000000);
        selected.setTextColor(0xFFFFFFFF);
        currentFilter = selected;
    }

    private void changeTab(TextView selected) {
        if (currentTab != null) {
            currentTab.setTypeface(null, android.graphics.Typeface.NORMAL);
            currentTab.setTextColor(0xFF666666);
        }
        selected.setTypeface(null, android.graphics.Typeface.BOLD);
        selected.setTextColor(0xFF000000);
        currentTab = selected;
    }

    /* ───────── Bottom Navigation ───────── */
    private void setupBottomNavigation() {
        navHome = findViewById(R.id.nav_home);
        navFriend = findViewById(R.id.nav_friend);
        navChat = findViewById(R.id.nav_chat);
        navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            Toast.makeText(this, "메인화면", Toast.LENGTH_SHORT).show();
        });

        navFriend.setOnClickListener(v -> safeLaunch(BoardActivity.class));

        navChat.setOnClickListener(v -> safeLaunch(ChatActivity.class));

        navProfile.setOnClickListener(v -> safeLaunch(ProfileActivity.class));
    }

    private void safeLaunch(Class<?> c) {
        if (LoginHelper.isLoggedIn(this)) {
            if (c.equals(PostWriteActivity.class)) {
                writeLauncher.launch(new Intent(this, c));
            } else {
                startActivity(new Intent(this, c));
            }
        } else {
            showLoginDialog();
        }
    }

    private void showLoginDialog() {
        new AlertDialog.Builder(this)
                .setTitle("로그인이 필요합니다")
                .setMessage("해당 기능은 로그인 후 이용할 수 있습니다.")
                .setPositiveButton("로그인하기", (d, w) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .setNegativeButton("닫기", null)
                .show();
    }
}