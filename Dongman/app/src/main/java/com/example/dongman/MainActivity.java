package com.example.dongman;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue; // FieldValue 임포트 (사용하지 않을 수도 있지만 필요에 따라)

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Date;

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

                // PostWriteActivity에서 이미 Firestore에 저장이 완료되었으므로,
                // MainActivity에서는 별도로 Post 객체를 받아 다시 저장할 필요가 없습니다.
                // PostWriteActivity가 성공적으로 종료되었는지 (RESULT_OK)만 확인하면 됩니다.
                if (r.getResultCode() == Activity.RESULT_OK) { // Changed to Activity.RESULT_OK for clarity
                    Toast.makeText(this, "게시물이 성공적으로 작성되었습니다!", Toast.LENGTH_SHORT).show();
                    // Firestore addSnapshotListener가 onStart에서 시작되고, 데이터 변경 시
                    // 자동으로 UI를 업데이트하므로 명시적인 새로고침 호출은 불필요합니다.
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
        // fabWrite 클릭 시 safeLaunch를 통해 로그인 체크 후 PostWriteActivity를 writeLauncher로 시작
        fabWrite.setOnClickListener(v -> safeLaunch(PostWriteActivity.class));

        // 앱 첫 실행 시에만 더미 데이터를 Firestore에 추가하도록 로직 수정
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean hasSeeded = prefs.getBoolean("hasSeeded", false);

        if (!hasSeeded) {
            db.collection("posts").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().isEmpty()) {
                    Log.d(TAG, "Firestore 'posts' collection is empty. Seeding dummy data.");
                    seedMeetingData();
                    // 더미 데이터 생성 후 플래그 저장 (apply() 권장)
                    prefs.edit().putBoolean("hasSeeded", true).apply();
                } else if (task.isSuccessful()) {
                    Log.d(TAG, "Firestore 'posts' collection is not empty. No seeding required. Count: " + task.getResult().size());
                } else {
                    Log.w(TAG, "Failed to check if 'posts' collection is empty: " + task.getException());
                }
            });
        } else {
            Log.d(TAG, "App already seeded. Skipping dummy data generation.");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // onStart에서 리스너를 시작하고, onStop에서 제거하여 메모리 누수 방지
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

    /**
     * Firestore에 더미 게시물을 저장하는 메서드.
     * Post 객체를 직접 Firestore에 추가합니다. (POJO 매핑 사용)
     */
    private void savePostToFirestore(Post post) {
        Log.d(TAG, "Attempting to save dummy post to Firestore: " + post.getTitle());

        // Post 객체를 직접 Firestore에 추가합니다.
        // Post 클래스에 public 필드 또는 public getter/setter가 있다면 Firestore가 자동으로 매핑합니다.
        // 더미 데이터에는 new Date()로 현재 시간을 넣습니다.
        db.collection("posts")
                .add(post) // Post 객체를 직접 저장
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Dummy DocumentSnapshot added with ID: " + documentReference.getId());
                    // 더미 데이터 저장 시에는 토스트 메시지가 불필요할 수 있습니다.
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding dummy document: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "더미 게시물 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Firestore에서 게시물 변경 사항을 실시간으로 감지하는 리스너 설정.
     */
    private void startListeningForPosts() {
        if (firestoreListener != null) {
            firestoreListener.remove();
            Log.d(TAG, "Existing Firestore listener removed before new one.");
        }

        // 필터 및 탭 선택에 따라 쿼리 변경 로직 추가
        Query baseQuery = db.collection("posts");

        // 현재 선택된 탭 (menuNew, menuRecommend)에 따른 정렬
        // Post.java에 'timestamp' 필드가 Date 타입으로 있어야 합니다.
        if (currentTab == menuNew) {
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING); // 최신순
        } else if (currentTab == menuRecommend) {
            // "인기순" 또는 "추천순" 정렬 로직을 추가하려면 Post 클래스에 해당 필드(예: 'views', 'likes')가 필요합니다.
            // 여기서는 임시로 timestamp 내림차순을 사용합니다.
            baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING);
        }

        // 현재 선택된 필터 (btnLatest, btnPopular, btnViews, btnNearby)에 따른 필터링 (where)
        // 이 부분은 Post 클래스의 필드와 일치하게 수정하거나, Firestore 문서에 추가 필드를 정의해야 합니다.
        if (currentFilter == btnLatest) {
            // 이미 timestamp 내림차순으로 정렬되므로 추가 필터링은 필요 없을 수 있습니다.
        } else if (currentFilter == btnPopular) {
            // 예시: Post 클래스에 'likes' 또는 'participants' 필드가 있다면
            // baseQuery = baseQuery.orderBy("likes", Query.Direction.DESCENDING).limit(10);
        } else if (currentFilter == btnViews) {
            // 예시: Post 클래스에 'views' 필드가 있다면
            // baseQuery = baseQuery.orderBy("views", Query.Direction.DESCENDING).limit(10);
        } else if (currentFilter == btnNearby) {
            // "주변" 필터링은 GeoFirestore와 같은 복잡한 구현이 필요하거나,
            // Post에 지역/도시 필드가 있다면 해당 필드로 whereEqualTo/whereArrayContains를 사용할 수 있습니다.
            // 예시: baseQuery = baseQuery.whereEqualTo("location", "청주시");
        }


        firestoreListener = baseQuery.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                Toast.makeText(this, "게시물 로딩 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshots != null) {
                // 기존 리스트를 비우고 변경사항을 반영하는 방식 (간단하나 효율은 떨어짐)
                meetingPosts.clear();
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    Post post = document.toObject(Post.class); // Firestore 문서 -> Post 객체로 변환
                    if (post != null) {
                        post.setId(document.getId()); // 문서 ID도 Post 객체에 저장
                        meetingPosts.add(post);
                    } else {
                        Log.w(TAG, "Failed to convert document " + document.getId() + " to Post object.");
                    }
                }
                // Firestore 쿼리에 orderBy가 있다면, 아래 수동 정렬은 필요 없습니다.
                // Collections.sort(meetingPosts, (p1, p2) -> {
                //     if (p1.getTimestamp() == null && p2.getTimestamp() == null) return 0;
                //     if (p1.getTimestamp() == null) return 1;
                //     if (p2.getTimestamp() == null) return -1;
                //     return p2.getTimestamp().compareTo(p1.getTimestamp());
                // });


                adapter.notifyDataSetChanged(); // 어댑터에 데이터 변경을 알립니다.
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
                    Post clickedPost = (Post) v.getTag(); // Adapter에서 setTag(post) 필요
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

        currentFilter = btnLatest; // 초기 필터 설정
        currentTab    = menuRecommend; // 초기 탭 설정
    }

    private void attachListeners() {
        // 필터 버튼 리스너
        View.OnClickListener filterListener = v -> {
            changeFilter((TextView) v);
            startListeningForPosts(); // 쿼리 변경 후 리스너 다시 시작 (새로운 필터 적용)
        };
        btnLatest.setOnClickListener(filterListener);
        btnPopular.setOnClickListener(filterListener);
        btnViews.setOnClickListener(filterListener);
        btnNearby.setOnClickListener(filterListener);

        // 탭 버튼 리스너
        View.OnClickListener tabListener = v -> {
            changeTab((TextView) v);
            startListeningForPosts(); // 쿼리 변경 후 리스너 다시 시작 (새로운 정렬 적용)
        };
        menuNew.setOnClickListener(tabListener);
        menuRecommend.setOnClickListener(tabListener);

        // 초기 필터와 탭의 UI 상태를 업데이트
        changeFilter(currentFilter);
        changeTab(currentTab);
    }

    private void changeFilter(TextView n) {
        if (currentFilter != null) { // Null 체크 추가
            currentFilter.setBackgroundColor(0xFFF5F5F5);
            currentFilter.setTextColor(0xFF666666);
        }
        n.setBackgroundColor(0xFF000000); // 검정색 배경
        n.setTextColor(0xFFFFFFFF); // 흰색 텍스트
        currentFilter = n;
    }

    private void changeTab(TextView n) {
        if (currentTab != null) { // Null 체크 추가
            currentTab.setTypeface(null, android.graphics.Typeface.NORMAL);
            currentTab.setTextColor(0xFF666666);
        }
        n.setTypeface(null, android.graphics.Typeface.BOLD);
        n.setTextColor(0xFF000000);
        currentTab = n;
    }

    /* ───────── 하단 네비 ───────── */
    private void setupBottomNavigation() {
        findViewById(R.id.nav_home   ).setOnClickListener(v -> {}); // 현재 Activity이므로 특별한 동작 없음
        findViewById(R.id.nav_friend ).setOnClickListener(v -> safeLaunch(BoardActivity.class));
        findViewById(R.id.nav_chat   ).setOnClickListener(v -> safeLaunch(ChatActivity.class));
        findViewById(R.id.nav_profile).setOnClickListener(v -> safeLaunch(ProfileActivity.class));
    }

    private void safeLaunch(Class<?> c){
        if(LoginHelper.isLoggedIn(this)){
            // PostWriteActivity를 호출하는 경우 writeLauncher를 사용합니다.
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

    /* ───────── 더미 데이터 ───────── */
    private void seedMeetingData() {
        String[] sports = {"자전거","테니스/스쿼시","볼링","배드민턴","스키/보드","골프","배구",
                "수영","요가","농구","클라이밍","축구","러닝/마라톤","야구","주짓수","검도",
                "헬스/크로스핏","승마","복싱","족구","다이어트"};
        Random r = new Random();

        String[] titleTemplates = {
                "함께 %s 즐겨요!",
                "%s 모임, 열정 넘치는 멤버 모집!",
                "초보 환영! 신나는 %s 한 판 하실 분?",
                "주말 %s 같이 하실 분 구합니다!",
                "%s 동호회에서 새 친구 만나요!",
                "오늘 저녁 %s 한 게임 어때요?",
                "건강하게 %s 운동하실 크루 모집!",
                "우리 동네 %s 같이 배우고 즐겨요!"
        };

        String[] timeOptions = getResources().getStringArray(R.array.time_options);

        // 더미 이미지 URL (placeholder.co 사용)
        String dummyImageUrl = "https://placehold.co/600x400/CCCCCC/000000?text=Dongman+Post+Image";


        for (int i = 0; i < 20; i++) {
            String selectedSport = sports[r.nextInt(sports.length)];
            String titleTemplate = titleTemplates[r.nextInt(titleTemplates.length)];
            String selectedTime = timeOptions[r.nextInt(timeOptions.length)];
            int randomCount = 5 + r.nextInt(10); // 5명에서 14명 사이

            Post p = new Post();
            p.setTitle(String.format(titleTemplate, selectedSport));
            // content 필드에 모임 소개 저장
            p.setContent(selectedSport + " 모임에 오신 것을 환영합니다! 상세 내용은 모임에 가입 후 확인해주세요.");
            p.setLocation("청주시"); // 장소를 특정 도시로 고정하거나 다양하게
            p.setTime(selectedTime);
            p.setCount(randomCount);
            // imageUrls 리스트에 더미 이미지 URL 추가
            p.setImageUrls(Collections.singletonList(dummyImageUrl)); // 단일 이미지 URL 리스트
            p.setTimestamp(new Date()); // 현재 시간으로 설정 (실제 앱에서는 FieldValue.serverTimestamp() 권장)

            savePostToFirestore(p);
        }
        Log.d(TAG, "Seed data generation complete.");
    }
}