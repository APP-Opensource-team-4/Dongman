package com.example.dongman;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences; // 더미 데이터 로직에서 사용되던 SharedPreferences는 이제 필요 없을 수 있음.
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections; // 더미 데이터에서 사용되던 Collections는 이제 필요 없을 수 있음.
// import java.util.Comparator; // 더미 데이터에서 사용되던 Comparator는 이제 필요 없을 수 있음.
import java.util.List;
// import java.util.Random; // 더미 데이터에서 사용되던 Random는 이제 필요 없을 수 있음.
// import java.util.Date; // 더미 데이터에서 사용되던 Date는 이제 필요 없을 수 있음.

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /* 리스트 데이터 */
    private final List<Post> meetingPosts = new ArrayList<>();
    private MeetingAdapter adapter;

    /* Firestore 인스턴스 */
    private FirebaseFirestore db;
    private ListenerRegistration firestoreListener;

    /* 중간 필터 / 상단 탭 */
    private TextView btnLatest, btnPopular, btnViews, btnNearby;
    private TextView menuNew, menuRecommend;
    private TextView currentFilter, currentTab;

    /* 글쓰기 결과 */
    private final ActivityResultLauncher<Intent> writeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                Log.d(TAG, "writeLauncher result received.");
                Log.d(TAG, "Result Code: " + r.getResultCode() + " (Expected: -1 for RESULT_OK)");
                Log.d(TAG, "Returned Intent data is null: " + (r.getData() == null));

                if (r.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(this, "게시물이 성공적으로 작성되었습니다!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "PostWriteActivity completed successfully. Post list should auto-refresh.");
                } else {
                    Log.w(TAG, "ActivityResult did not return RESULT_OK. Result Code: " + r.getResultCode());
                    Toast.makeText(this, "게시물 작성 취소 또는 오류 발생.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        bindViews();
        attachListeners();
        setupRecycler();
        setupBottomNavigation();

        ExtendedFloatingActionButton fabWrite = findViewById(R.id.btn_write);
        fabWrite.setOnClickListener(v -> safeLaunch(PostWriteActivity.class));

        // 더미 데이터 생성 로직은 이제 제거됩니다.
        // 앱 시작 시 로그인 상태 확인 및 더미 데이터 생성/확인 로직도 제거.
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
            Log.d(TAG, "Firestore listener removed.");
        }
    }

    // savePostToFirestore 메서드는 PostWriteActivity에서 호출될 것이므로 유지.
    // 하지만 MainActivity에서 직접적으로 더미 포스트를 저장할 필요는 없어졌습니다.
    private void savePostToFirestore(Post post) {
        Log.d(TAG, "Attempting to save post to Firestore: " + post.getTitle());

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "게시물 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void startListeningForPosts() {
        if (firestoreListener != null) {
            firestoreListener.remove();
            Log.d(TAG, "Existing Firestore listener removed before new one.");
        }

        Query baseQuery = db.collection("posts");

        if (currentTab == menuNew) {
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING);
        } else if (currentTab == menuRecommend) {
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING);
        }

        if (currentFilter == btnLatest) {
            // No additional filter needed if already ordered by timestamp
        } else if (currentFilter == btnPopular) {
            // Example: baseQuery = baseQuery.orderBy("likes", Query.Direction.DESCENDING).limit(10);
        } else if (currentFilter == btnViews) {
            // Example: baseQuery = baseQuery.orderBy("views", Query.Direction.DESCENDING).limit(10);
        } else if (currentFilter == btnNearby) {
            // Example: baseQuery = baseQuery.whereEqualTo("location", "청주시");
        }


        firestoreListener = baseQuery.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                Toast.makeText(this, "게시물 로딩 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshots != null) {
                meetingPosts.clear();
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    Post post = document.toObject(Post.class);
                    if (post != null) {
                        post.setId(document.getId());
                        meetingPosts.add(post);
                    } else {
                        Log.w(TAG, "Failed to convert document " + document.getId() + " to Post object.");
                    }
                }
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Posts loaded/updated in UI: " + meetingPosts.size() + " items");

                if (!meetingPosts.isEmpty()) {
                    ((RecyclerView) findViewById(R.id.rv_meetings)).scrollToPosition(0);
                }
            }
        });
    }

    /* ───────── RecyclerView ───────── */
    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rv_meetings);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MeetingAdapter(meetingPosts,
                v -> {
                    Post clickedPost = (Post) v.getTag();
                    if (clickedPost != null) {
                        Intent detailIntent = new Intent(this, DetailActivity.class);
                        detailIntent.putExtra("post", clickedPost);
                        startActivity(detailIntent);
                    } else {
                        Log.e(TAG, "Clicked post is null. Check adapter's setTag.");
                        Toast.makeText(this, "게시물 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
        rv.setAdapter(adapter);
    }

    /* ───────── 버튼 바인딩/리스너 ───────── */
    private void bindViews() {
        btnLatest  = findViewById(R.id.btn_latest);
        btnPopular = findViewById(R.id.btn_popular);
        btnViews   = findViewById(R.id.btn_views);
        btnNearby  = findViewById(R.id.btn_nearby);

        menuNew       = findViewById(R.id.menu_new);
        menuRecommend = findViewById(R.id.menu_recommend);

        currentFilter = btnLatest;
        currentTab    = menuRecommend;
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

        changeFilter(currentFilter);
        changeTab(currentTab);
    }

    private void changeFilter(TextView n) {
        if (currentFilter != null) {
            currentFilter.setBackgroundColor(0xFFF5F5F5);
            currentFilter.setTextColor(0xFF666666);
        }
        n.setBackgroundColor(0xFF000000);
        n.setTextColor(0xFFFFFFFF);
        currentFilter = n;
    }

    private void changeTab(TextView n) {
        if (currentTab != null) {
            currentTab.setTypeface(null, android.graphics.Typeface.NORMAL);
            currentTab.setTextColor(0xFF666666);
        }
        n.setTypeface(null, android.graphics.Typeface.BOLD);
        n.setTextColor(0xFF000000);
        currentTab = n;
    }

    /* ───────── 하단 네비 ───────── */
    private void setupBottomNavigation() {
        findViewById(R.id.nav_home   ).setOnClickListener(v -> {});
        findViewById(R.id.nav_friend ).setOnClickListener(v -> safeLaunch(BoardActivity.class));
        findViewById(R.id.nav_chat   ).setOnClickListener(v -> safeLaunch(ChatActivity.class));
        findViewById(R.id.nav_profile).setOnClickListener(v -> safeLaunch(ProfileActivity.class));
    }

    private void safeLaunch(Class<?> c){
        // Pass 'this' (the MainActivity context) to the isLoggedIn method.
        if(LoginHelper.isLoggedIn(this)){
            if (c.equals(PostWriteActivity.class)) {
                writeLauncher.launch(new Intent(this, c));
            } else {
                startActivity(new Intent(this, c));
            }
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("로그인이 필요합니다")
                    .setMessage("해당 기능은 로그인 후 이용할 수 있습니다.")
                    .setPositiveButton("로그인하기",
                            (d,w)->startActivity(new Intent(this,LoginActivity.class)))
                    .setNegativeButton("닫기",null)
                    .show();
        }
    }

    /* ───────── 더미 데이터 (이제 필요 없음) ───────── */
    // seedMeetingData 메서드와 관련된 필드 및 호출을 모두 제거합니다.
    // 기존 seedMeetingData() 메서드 자체는 이 파일에서 제거됩니다.
}