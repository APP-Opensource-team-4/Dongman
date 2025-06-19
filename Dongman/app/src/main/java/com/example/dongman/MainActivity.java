package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private MeetingAdapter adapter;
    private final List<Post> postList = new ArrayList<>();
    private ProgressBar loadingBar;

    // 하단 네비게이션
    private LinearLayout navHome, navFriend, navChat, navProfile;

    // 글쓰기 버튼
    private ExtendedFloatingActionButton btnWrite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recycler_view);
        loadingBar = findViewById(R.id.loading_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MeetingAdapter(postList, this::onPostClick);
        recyclerView.setAdapter(adapter);

        btnWrite = findViewById(R.id.btn_write);
        btnWrite.setOnClickListener(v -> {
            if (LoginHelper.isLoggedIn(this)) {
                startActivity(new Intent(MainActivity.this, PostWriteActivity.class));
            } else {
                toast("로그인이 필요합니다.");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        loadPostsFromFirestore();
        setupBottomNavigation();
    }

    private void loadPostsFromFirestore() {
        loadingBar.setVisibility(View.VISIBLE);
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setId(doc.getId());

                            if (post.imageUrls == null) {
                                post.imageUrls = new ArrayList<>();
                            }

                            postList.add(post);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    loadingBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "데이터 불러오기 실패", e);
                    loadingBar.setVisibility(View.GONE);
                });
    }

    private void onPostClick(View view) {
        Post clickedPost = (Post) view.getTag();
        if (clickedPost != null && clickedPost.getId() != null) {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("postId", clickedPost.getId());
            startActivity(intent);
        }
    }

    private void setupBottomNavigation() {
        navHome = findViewById(R.id.nav_home);
        navFriend = findViewById(R.id.nav_friend);
        navChat = findViewById(R.id.nav_chat);
        navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            // 현재 화면이 홈이므로 아무 작업도 하지 않음
        });

        navFriend.setOnClickListener(v -> {
            if (LoginHelper.isLoggedIn(this)) {
                startActivity(new Intent(MainActivity.this, BoardActivity.class));
            } else {
                toast("로그인이 필요합니다.");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        navChat.setOnClickListener(v -> {
            if (LoginHelper.isLoggedIn(this)) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class));
            } else {
                toast("로그인이 필요합니다.");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        navProfile.setOnClickListener(v -> {
            if (LoginHelper.isLoggedIn(this)) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else {
                toast("로그인이 필요합니다.");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
