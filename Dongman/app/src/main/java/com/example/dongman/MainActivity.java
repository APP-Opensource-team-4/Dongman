package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Log import 추가
import android.view.View;
import android.widget.TextView;
import android.widget.Toast; // Toast import 추가

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

// ✨ Firestore 관련 Import 추가
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration; // 리스너 등록 해제를 위해

import java.util.ArrayList;
import java.util.Collections; // 리스트 정렬을 위해
import java.util.Comparator; // 리스트 정렬을 위해
import java.util.List;
import java.util.Random;
import java.util.Date; // Date 클래스 임포트

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // 로그 태그

    /* 리스트 데이터 */
    private final List<Post> meetingPosts = new ArrayList<>();
    private MeetingAdapter adapter;

    /* Firestore 인스턴스 */
    private FirebaseFirestore db; // ✨ Firestore 인스턴스 선언
    private ListenerRegistration firestoreListener; // ✨ 실시간 리스너 등록 해제를 위한 변수

    /* 중간 필터 / 상단 탭 */
    private TextView btnLatest, btnPopular, btnViews, btnNearby;
    private TextView menuNew, menuRecommend;
    private TextView currentFilter, currentTab;

    /* 글쓰기 결과 */
    private final ActivityResultLauncher<Intent> writeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK && r.getData() != null) {
                    Post p = (Post) r.getData().getSerializableExtra("post");
                    if (p != null) {
                        // ✨ 게시물을 Firestore에 저장
                        savePostToFirestore(p);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        // ✨ Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        bindViews();
        attachListeners();
        setupRecycler();
        setupBottomNavigation();

        // ✨ Firestore에서 게시물 로드 시작
        startListeningForPosts();

        ExtendedFloatingActionButton fabWrite = findViewById(R.id.btn_write);
        fabWrite.setOnClickListener(v ->
                writeLauncher.launch(new Intent(this, PostWriteActivity.class)));

        // ✨ (선택 사항) 앱 첫 실행 시에만 더미 데이터를 Firestore에 추가
        // 이 부분은 필요에 따라 조정하세요.
        // 예를 들어, 앱 시작 시 1회만 실행되도록 SharedPreferences에 플래그를 저장하여 관리할 수 있습니다.
        // 현재는 매번 실행될 때마다 DB에 게시물이 없으면 추가하므로 테스트용으로 적합합니다.
        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                seedMeetingData();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 액티비티가 시작될 때 리스너가 연결되었는지 확인하고 연결
        if (firestoreListener == null) {
            startListeningForPosts();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 액티비티가 중지될 때 리스너 해제 (메모리 누수 방지)
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
    }

    /**
     * ✨ Firestore에 게시물을 저장하는 메서드
     */
    private void savePostToFirestore(Post post) {
        db.collection("posts")
                .add(post) // Post 객체를 Firestore 문서로 추가 (자동 ID 생성)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    // Firestore에 저장된 후, Post 객체에 ID를 설정할 수 있습니다.
                    post.id = documentReference.getId(); // 게시물 객체에 Firestore ID 저장
                    // UI 업데이트는 addSnapshotListener가 처리하므로 별도로 하지 않습니다.
                    Toast.makeText(MainActivity.this, "게시물이 성공적으로 작성되었습니다!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(MainActivity.this, "게시물 작성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * ✨ Firestore에서 게시물 변경 사항을 실시간으로 감지하는 리스너 설정
     */
    private void startListeningForPosts() {
        firestoreListener = db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING) // ✨ timestamp 기준으로 최신순 정렬
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Toast.makeText(this, "게시물 로딩 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Post post = dc.getDocument().toObject(Post.class);
                            post.id = dc.getDocument().getId(); // Firestore 문서 ID를 Post 객체에 저장

                            switch (dc.getType()) {
                                case ADDED:
                                    // 새로 추가된 게시물 (가장 위에 삽입)
                                    meetingPosts.add(0, post);
                                    adapter.notifyItemInserted(0);
                                    Log.d(TAG, "New post: " + post.title);
                                    break;
                                case MODIFIED:
                                    // 수정된 게시물 (기존 위치 찾아서 업데이트)
                                    // Room처럼 인덱스가 보장되지 않으므로, ID로 찾아서 업데이트해야 합니다.
                                    for (int i = 0; i < meetingPosts.size(); i++) {
                                        if (meetingPosts.get(i).id.equals(post.id)) {
                                            meetingPosts.set(i, post);
                                            adapter.notifyItemChanged(i);
                                            Log.d(TAG, "Modified post: " + post.title);
                                            break;
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    // 삭제된 게시물 (기존 위치 찾아서 제거)
                                    for (int i = 0; i < meetingPosts.size(); i++) {
                                        if (meetingPosts.get(i).id.equals(post.id)) {
                                            meetingPosts.remove(i);
                                            adapter.notifyItemRemoved(i);
                                            Log.d(TAG, "Removed post: " + post.title);
                                            break;
                                        }
                                    }
                                    break;
                            }
                        }
                        // 변경사항이 있을 때마다 리스트를 다시 정렬 (timestamp 기준)
                        // addSnapshotListener의 DocumentChange는 변경 유형별로 오지만, 순서가 항상 보장되지 않을 수 있습니다.
                        // 특히 복잡한 필터링이나 정렬이 있다면, 전체 데이터를 불러와 정렬하는 방식이 더 안정적입니다.
                        // 여기서는 간단한 추가/삭제/수정으로 처리하지만, 필요에 따라 전체 리로드를 고려할 수 있습니다.
                        // Collections.sort(meetingPosts, (p1, p2) -> p2.timestamp.compareTo(p1.timestamp));
                        // adapter.notifyDataSetChanged();
                    }
                });
    }


    /* ───────── RecyclerView ───────── */
    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rv_meetings);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MeetingAdapter(meetingPosts,
                v -> {
                    // DetailActivity로 이동하며 Post 객체 전달
                    Post clickedPost = (Post) v.getTag();
                    Intent detailIntent = new Intent(this, DetailActivity.class);
                    detailIntent.putExtra("post", clickedPost);
                    startActivity(detailIntent);
                });
        rv.setAdapter(adapter);
    }

    // ✨ addPost 메서드는 더 이상 사용하지 않으므로 제거
    /*
    private void addPost(@NonNull Post p) {
        meetingPosts.add(0, p);
        adapter.notifyItemInserted(0);
        ((RecyclerView) findViewById(R.id.rv_meetings)).scrollToPosition(0);
    }
    */

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
        View.OnClickListener f = v -> {
            changeFilter((TextView) v);
            // ✨ 필터링 로직: Firestore 쿼리를 변경하여 데이터를 다시 로드
            // 예시: db.collection("posts").whereEqualTo("category", "스포츠")...
            startListeningForPosts(); // 쿼리를 변경한 후 다시 리스너 시작
        };
        btnLatest.setOnClickListener(f);
        btnPopular.setOnClickListener(f);
        btnViews.setOnClickListener(f);
        btnNearby.setOnClickListener(f);

        View.OnClickListener t = v -> {
            changeTab((TextView) v);
            // ✨ 정렬/탭 로직: Firestore 쿼리의 정렬 기준을 변경하여 데이터를 다시 로드
            // 예시: db.collection("posts").orderBy("views", Query.Direction.DESCENDING)...
            startListeningForPosts(); // 쿼리를 변경한 후 다시 리스너 시작
        };
        menuNew.setOnClickListener(t);
        menuRecommend.setOnClickListener(t);
    }

    private void changeFilter(TextView n) {
        currentFilter.setBackgroundColor(0xFFF5F5F5);
        currentFilter.setTextColor(0xFF666666);
        n.setBackgroundColor(0xFF000000);
        n.setTextColor(0xFFFFFFFF);
        currentFilter = n;
    }

    private void changeTab(TextView n) {
        currentTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        currentTab.setTextColor(0xFF666666);
        n.setTypeface(null, android.graphics.Typeface.BOLD);
        n.setTextColor(0xFF000000);
        currentTab = n;
    }

    /* ───────── 하단 네비 ───────── */
    private void setupBottomNavigation() {
        findViewById(R.id.nav_home   ).setOnClickListener(v -> {}); // 현재 화면
        findViewById(R.id.nav_friend ).setOnClickListener(v -> safeLaunch(BoardActivity.class));
        findViewById(R.id.nav_chat   ).setOnClickListener(v -> safeLaunch(ChatActivity.class));
        findViewById(R.id.nav_profile).setOnClickListener(v -> safeLaunch(ProfileActivity.class));
    }

    /** 로그인 필요 기능 실행용 공통 메서드 */
    // ✨ 화면 아무 곳 터치 시 로그인 체크 메서드 (dispatchTouchEvent) 제거 (가장 중요)
    // 이 메서드는 앱 동작에 여러 문제를 일으킬 수 있습니다.
    /*
    @Override public boolean dispatchTouchEvent(@NonNull android.view.MotionEvent e){
        if(!LoginHelper.isLoggedIn(this)&&e.getAction()==android.view.MotionEvent.ACTION_DOWN){
            new AlertDialog.Builder(this)
                    .setTitle("로그인이 필요합니다")
                    .setMessage("해당 기능은 로그인 후 이용할 수 있습니다.")
                    .setPositiveButton("로그인하기",
                            (d,w)->startActivity(new Intent(this,LoginActivity.class)))
                    .setNegativeButton("닫기",null)
                    .show();
            return true;
        }
        return super.dispatchTouchEvent(e);
    }
    */

    // safeLaunch는 그대로 유지
    private void safeLaunch(Class<?> c){
        if(LoginHelper.isLoggedIn(this)){
            startActivity(new Intent(this,c));
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
        String[] sport = {"자전거","테니스/스쿼시","볼링","배드민턴","스키/보드","골프","배구",
                "수영","요가","농구","클라이밍","축구","러닝/마라톤","야구","주짓수","검도",
                "헬스/크로스핏","승마","복싱","족구","다이어트"};
        Random r = new Random();
        for (int i = 0; i < 20; i++) {
            Post p = new Post();
            p.title    = "임시 제목 " + (i + 1);
            p.meta     = "임시 내용 " + (i + 1);
            p.location = sport[r.nextInt(sport.length)] + " • 청주시 • 멤버 " + (5 + i) + "명";
            p.imageRes = R.drawable.placeholder_thumbnail;
            p.timestamp = new Date(System.currentTimeMillis() - (i * 1000 * 60)); // ✨ 시간 역순으로 더미 데이터 생성
            savePostToFirestore(p); // ✨ 더미 데이터도 Firestore에 저장
        }
    }
}