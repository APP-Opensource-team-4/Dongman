package com.example.dongman;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    private static final String TAG = "MyPostsActivity";

    // UI 요소
    private RecyclerView recyclerView;
    private ProgressBar loadingBar;
    private MyPostsAdapter adapter;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    // 데이터
    private List<Post> myPostsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // UI 초기화
        initializeUI();

        // 내 게시물 로드
        loadMyPosts();
    }

    private void initializeUI() {
        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("내 모임");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recycler_view);
        loadingBar = findViewById(R.id.loading_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 어댑터 초기화 (수정 기능 제거, 삭제만 유지)
        adapter = new MyPostsAdapter(
                myPostsList,
                this::onPostClick,     // 게시물 클릭 (상세 보기)
                this::onDeleteClick    // 삭제 클릭
        );
        recyclerView.setAdapter(adapter);
    }

    private void loadMyPosts() {
        // LoginHelper로 먼저 체크 (PostWriteActivity와 동일한 방식)
        if (!LoginHelper.isLoggedIn(this)) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserUid = getUserId(currentUser);

        Log.d(TAG, "=== 내 모임 로드 ===");
        Log.d(TAG, "LoginHelper.isLoggedIn: " + LoginHelper.isLoggedIn(this));
        Log.d(TAG, "Firebase User: " + currentUser);
        Log.d(TAG, "Current UID: " + currentUserUid);

        loadingBar.setVisibility(View.VISIBLE);

        // 현재 사용자 UID로 작성한 게시물만 조회 (orderBy 임시 제거)
        db.collection("posts")
                .whereEqualTo("hostUid", currentUserUid)
                // .orderBy("timestamp", Query.Direction.DESCENDING)  // 인덱스 생성까지 임시 주석
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myPostsList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Post post = document.toObject(Post.class);
                        if (post != null) {
                            post.setId(document.getId());
                            if (post.getImageUrls() == null) {
                                post.setImageUrls(new ArrayList<>());
                            }
                            myPostsList.add(post);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    loadingBar.setVisibility(View.GONE);

                    Log.d(TAG, "Loaded " + myPostsList.size() + " posts for UID: " + currentUserUid);

                    if (myPostsList.isEmpty()) {
                        Toast.makeText(this, "작성한 모임이 없습니다", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load posts", e);
                    Toast.makeText(this, "게시물 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingBar.setVisibility(View.GONE);
                });
    }

    // PostWriteActivity와 동일한 getUserId 메서드 추가
    private String getUserId(FirebaseUser user) {
        if (user != null && user.getUid() != null) {
            return user.getUid();
        }

        // SharedPreferences에서 저장된 anonymous UID 가져오기
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String savedUid = prefs.getString("anonymous_uid", null);

        if (savedUid == null) {
            // 처음 생성하는 경우만 새로 만들고 저장
            savedUid = "anonymous_" + System.currentTimeMillis();
            prefs.edit().putString("anonymous_uid", savedUid).apply();
            Log.d(TAG, "새로운 anonymous UID 생성: " + savedUid);
        } else {
            Log.d(TAG, "기존 anonymous UID 사용: " + savedUid);
        }

        return savedUid;
    }

    // 게시물 클릭 - 상세 보기
    private void onPostClick(Post post) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("postId", post.getId());
        startActivity(intent);
    }

    // 삭제 버튼 클릭
    private void onDeleteClick(Post post) {
        new AlertDialog.Builder(this)
                .setTitle("모임 삭제")
                .setMessage("정말로 이 모임을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deletePost(post))
                .setNegativeButton("취소", null)
                .show();
    }

    private void deletePost(Post post) {
        loadingBar.setVisibility(View.VISIBLE);

        // Firestore에서 게시물 삭제
        db.collection("posts").document(post.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Storage에서 이미지들 삭제
                    deletePostImages(post);

                    // 로컬 리스트에서 제거
                    myPostsList.remove(post);
                    adapter.notifyDataSetChanged();

                    loadingBar.setVisibility(View.GONE);
                    Toast.makeText(this, "모임이 삭제되었습니다", Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Post deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete post", e);
                    Toast.makeText(this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingBar.setVisibility(View.GONE);
                });
    }

    private void deletePostImages(Post post) {
        if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
            for (String imageUrl : post.getImageUrls()) {
                try {
                    StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
                    imageRef.delete()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Image deleted: " + imageUrl))
                            .addOnFailureListener(e -> Log.w(TAG, "Failed to delete image: " + imageUrl, e));
                } catch (Exception e) {
                    Log.w(TAG, "Invalid image URL: " + imageUrl, e);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 화면이 다시 보일 때 새로고침
        loadMyPosts();
    }
}