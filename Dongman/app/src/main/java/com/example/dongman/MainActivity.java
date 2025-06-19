// MainActivity.java
package com.example.dongman;

import android.app.Activity; // Import for Activity.RESULT_OK
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView; // Added for filter/tab TextViews
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // Added for ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts; // Added for ActivityResultContracts
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Added for AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException; // Added for FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration; // Added for ListenerRegistration
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot; // Added for QuerySnapshot

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth; // Added for FirebaseAuth
import com.google.firebase.auth.FirebaseUser; // Added for FirebaseUser


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // TAG for logging

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private MeetingAdapter adapter;
    private final List<Post> postList = new ArrayList<>(); // Renamed from meetingPosts for consistency
    private ProgressBar loadingBar;
    private ListenerRegistration firestoreListener; // Firestore real-time listener

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
                Log.d(TAG, "writeLauncher result received.");
                Log.d(TAG, "Result Code: " + r.getResultCode() + " (Expected: -1 for RESULT_OK)");
                Log.d(TAG, "Returned Intent data is null: " + (r.getData() == null));

                if (r.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(this, "게시물이 성공적으로 작성되었습니다!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "PostWriteActivity completed successfully. Post list should auto-refresh.");
                    // No need to manually reload, listener handles it
                } else {
                    Log.w(TAG, "ActivityResult did not return RESULT_OK. Result Code: " + r.getResultCode());
                    Toast.makeText(this, "게시물 작성 취소 또는 오류 발생.", Toast.LENGTH_SHORT).show();
                }
            });

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
        btnWrite.setOnClickListener(v -> safeLaunch(PostWriteActivity.class));

        bindViews(); // Initialize filter and tab TextViews
        attachListeners(); // Attach listeners to filter and tab TextViews
        setupBottomNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListeningForPosts(); // Start listening for real-time updates
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

    private void startListeningForPosts() {
        if (firestoreListener != null) {
            firestoreListener.remove();
            Log.d(TAG, "Existing Firestore listener removed before new one.");
        }

        loadingBar.setVisibility(View.VISIBLE);
        Query baseQuery = db.collection("posts");

        // Apply tab filtering
        if (currentTab == menuNew) {
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING);
        } else if (currentTab == menuRecommend) {
            // For recommendation, you might want a different logic or just default to latest
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING);
        }

        // Apply filter buttons (example logic, extend as needed)
        if (currentFilter == btnLatest) {
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING);
        } else if (currentFilter == btnPopular) {
            // Example: Order by a 'likes' field or similar for popularity
            // baseQuery = baseQuery.orderBy("likes", Query.Direction.DESCENDING);
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING); // Fallback
            Toast.makeText(this, "인기순 필터 (구현 예정)", Toast.LENGTH_SHORT).show();
        } else if (currentFilter == btnViews) {
            // Example: Order by a 'views' field
            // baseQuery = baseQuery.orderBy("views", Query.Direction.DESCENDING);
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING); // Fallback
            Toast.makeText(this, "조회순 필터 (구현 예정)", Toast.LENGTH_SHORT).show();
        } else if (currentFilter == btnNearby) {
            // Example: Filter by location (requires more complex logic for actual "nearby")
            // For now, let's just show a toast or filter by a specific location
            // baseQuery = baseQuery.whereEqualTo("location", "청주시");
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING); // Fallback
            Toast.makeText(this, "가까운순 필터 (구현 예정)", Toast.LENGTH_SHORT).show();
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
                        // Ensure imageUrls list is initialized if it comes null from Firestore
                        if (post.getImageUrls() == null) {
                            post.setImageUrls(new ArrayList<>());
                        }
                        postList.add(post);
                    } else {
                        Log.w(TAG, "Failed to convert document " + document.getId() + " to Post object.");
                    }
                }
                adapter.notifyDataSetChanged();
                loadingBar.setVisibility(View.GONE);
                Log.d(TAG, "Posts loaded/updated in UI: " + postList.size() + " items");

                if (!postList.isEmpty()) {
                    recyclerView.scrollToPosition(0); // Scroll to top on update
                }
            }
        });
    }

    private void onPostClick(View view) {
        Post clickedPost = (Post) view.getTag();
        if (clickedPost != null && clickedPost.getId() != null) {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("postId", clickedPost.getId()); // Pass postId
            startActivity(intent);
        } else {
            Log.e(TAG, "Clicked post or its ID is null. Check adapter's setTag.");
            Toast.makeText(this, "게시물 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /* ───────── UI Binding and Listeners for Filters/Tabs ───────── */
    private void bindViews() {
        btnLatest  = findViewById(R.id.btn_latest);
        btnPopular = findViewById(R.id.btn_popular);
        btnViews   = findViewById(R.id.btn_views);
        btnNearby  = findViewById(R.id.btn_nearby);

        menuNew       = findViewById(R.id.menu_new);
        menuRecommend = findViewById(R.id.menu_recommend);

        // Set initial selected filter and tab
        currentFilter = btnLatest;
        currentTab    = menuRecommend; // Or menuNew, based on default view
    }

    private void attachListeners() {
        View.OnClickListener filterListener = v -> {
            changeFilter((TextView) v);
            startListeningForPosts(); // Reload posts based on new filter
        };
        btnLatest.setOnClickListener(filterListener);
        btnPopular.setOnClickListener(filterListener);
        btnViews.setOnClickListener(filterListener);
        btnNearby.setOnClickListener(filterListener);

        View.OnClickListener tabListener = v -> {
            changeTab((TextView) v);
            startListeningForPosts(); // Reload posts based on new tab
        };
        menuNew.setOnClickListener(tabListener);
        menuRecommend.setOnClickListener(tabListener);

        // Apply initial styles
        changeFilter(currentFilter);
        changeTab(currentTab);
    }

    private void changeFilter(TextView n) {
        if (currentFilter != null) {
            currentFilter.setBackgroundColor(0xFFF5F5F5); // Light gray
            currentFilter.setTextColor(0xFF666666);      // Dark gray text
        }
        n.setBackgroundColor(0xFF000000); // Black background
        n.setTextColor(0xFFFFFFFF);      // White text
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

    /* ───────── Bottom Navigation ───────── */
    private void setupBottomNavigation() {
        navHome = findViewById(R.id.nav_home);
        navFriend = findViewById(R.id.nav_friend);
        navChat = findViewById(R.id.nav_chat);
        navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            // Currently on Home, do nothing or refresh if needed
            Toast.makeText(this, "홈", Toast.LENGTH_SHORT).show();
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
            new AlertDialog.Builder(this)
                    .setTitle("로그인이 필요합니다")
                    .setMessage("해당 기능은 로그인 후 이용할 수 있습니다.")
                    .setPositiveButton("로그인하기",
                            (d, w) -> startActivity(new Intent(this, LoginActivity.class)))
                    .setNegativeButton("닫기", null)
                    .show();
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}