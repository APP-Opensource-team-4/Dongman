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

// Firestore 관련 Import
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentChange; // DocumentChange 처리용
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration; // 리스너 등록 해제를 위해
import com.google.firebase.firestore.DocumentSnapshot; // DocumentSnapshot 임포트
import com.google.firebase.firestore.FieldValue; // FieldValue 임포트 (서버 타임스탬프용)

import java.util.ArrayList;
import java.util.Collections; // 리스트 정렬을 위해
import java.util.Comparator; // 리스트 정렬을 위해
import java.util.HashMap; // Map 사용을 위해
import java.util.List;
import java.util.Map; // Map 사용을 위해
import java.util.Random;
import java.util.Date; // Date 클래스 임포트 (Post 객체에서 사용)

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // 로그 태그

    /* 리스트 데이터 */
    private final List<Post> meetingPosts = new ArrayList<>();
    private MeetingAdapter adapter;

    /* Firestore 인스턴스 */
    private FirebaseFirestore db; // Firestore 인스턴스 선언
    private ListenerRegistration firestoreListener; // 실시간 리스너 등록 해제를 위한 변수

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
                        // 게시물을 Firestore에 저장
                        savePostToFirestore(p);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        bindViews();
        attachListeners();
        setupRecycler();
        setupBottomNavigation();

        // ExtendedFloatingActionButton (글쓰기 버튼)에 safeLaunch 적용
        ExtendedFloatingActionButton fabWrite = findViewById(R.id.btn_write);
        fabWrite.setOnClickListener(v ->
                safeLaunch(PostWriteActivity.class));

        // (선택 사항) 앱 첫 실행 시에만 더미 데이터를 Firestore에 추가
        // Firestore에 "posts" 컬렉션이 비어있을 때만 더미 데이터 생성
        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                seedMeetingData();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 액티비티가 시작될 때 리스너를 시작합니다.
        // onStop()에서 리스너를 해제하므로, 화면에 다시 나타날 때마다 리스너를 다시 연결해야 합니다.
        if (firestoreListener == null) {
            startListeningForPosts();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 액티비티가 중지될 때 리스너를 해제합니다. (메모리 누수 방지 및 불필요한 데이터 로드 방지)
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
    }

    /**
     * Firestore에 게시물을 저장하는 메서드.
     * FieldValue.serverTimestamp()를 사용하여 서버 시간을 적용합니다.
     */
    private void savePostToFirestore(Post post) {
        // Post 객체를 Map으로 변환하여 Firestore에 보낼 데이터를 준비합니다.
        // FieldValue.serverTimestamp()는 POJO에 직접 할당할 수 없으므로 Map을 사용합니다.
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", post.title);
        postMap.put("meta", post.meta);
        postMap.put("location", post.location);
        postMap.put("imageRes", post.imageRes);
        postMap.put("timestamp", FieldValue.serverTimestamp()); // ✨ 서버 타임스탬프 적용

        db.collection("posts")
                .add(postMap) // Map 형태로 데이터 추가 (자동 ID 생성)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    // UI 업데이트는 addSnapshotListener가 처리하므로, 여기서 meetingPosts에 추가할 필요 없습니다.
                    Toast.makeText(MainActivity.this, "게시물이 성공적으로 작성되었습니다!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(MainActivity.this, "게시물 작성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Firestore에서 게시물 변경 사항을 실시간으로 감지하는 리스너 설정.
     * DocumentChange를 사용하여 효율적으로 RecyclerView를 업데이트합니다.
     */
    private void startListeningForPosts() {
        // 기존 리스너가 있다면 먼저 해제하여 중복 실행을 방지
        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        firestoreListener = db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING) // timestamp 기준으로 최신순 정렬
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Toast.makeText(this, "게시물 로딩 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Post post = dc.getDocument().toObject(Post.class);
                            if (post != null) {
                                post.id = dc.getDocument().getId(); // Firestore 문서 ID를 Post 객체에 저장

                                switch (dc.getType()) {
                                    case ADDED:
                                        // 새로 추가된 게시물 (리스트에 추가)
                                        meetingPosts.add(post);
                                        Log.d(TAG, "New post: " + post.title);
                                        break;
                                    case MODIFIED:
                                        // 수정된 게시물 (기존 위치 찾아서 업데이트)
                                        for (int i = 0; i < meetingPosts.size(); i++) {
                                            if (meetingPosts.get(i).id.equals(post.id)) {
                                                meetingPosts.set(i, post);
                                                Log.d(TAG, "Modified post: " + post.title);
                                                break;
                                            }
                                        }
                                        break;
                                    case REMOVED:
                                        // 삭제된 게시물 (기존 위치 찾아서 제거)
                                        meetingPosts.removeIf(pItem -> pItem.id.equals(post.id));
                                        Log.d(TAG, "Removed post: " + post.title);
                                        break;
                                }
                            }
                        }
                        // 모든 변경 사항 처리 후 리스트를 다시 정렬
                        // addSnapshotListener의 DocumentChange는 변경 유형별로 오지만,
                        // 정렬 순서가 보장되지 않을 수 있으므로 전체를 다시 정렬하는 것이 안전합니다.
                        Collections.sort(meetingPosts, (p1, p2) -> {
                            // null 체크를 하여 NullPointerException 방지
                            if (p1.timestamp == null && p2.timestamp == null) return 0;
                            if (p1.timestamp == null) return 1; // p1이 null이면 p2가 더 크다고 간주 (p1을 뒤로)
                            if (p2.timestamp == null) return -1; // p2가 null이면 p1이 더 크다고 간주 (p2를 뒤로)
                            return p2.timestamp.compareTo(p1.timestamp); // 내림차순 정렬 (최신순)
                        });

                        adapter.notifyDataSetChanged(); // UI 갱신
                        Log.d(TAG, "Posts loaded/updated: " + meetingPosts.size() + " items");

                        if (!meetingPosts.isEmpty()) {
                            // RecyclerView를 가장 최신 게시물 위치로 스크롤
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
                    // DetailActivity로 이동하며 Post 객체 전달
                    Post clickedPost = (Post) v.getTag();
                    Intent detailIntent = new Intent(this, DetailActivity.class);
                    detailIntent.putExtra("post", clickedPost);
                    startActivity(detailIntent);
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
        View.OnClickListener f = v -> {
            changeFilter((TextView) v);
            // 필터링 로직: 여기에 Firestore 쿼리 (예: .whereEqualTo("category", "스포츠"))를 변경
            startListeningForPosts(); // 쿼리 변경 후 리스너 다시 시작 (새로운 필터 적용)
        };
        btnLatest.setOnClickListener(f);
        btnPopular.setOnClickListener(f);
        btnViews.setOnClickListener(f);
        btnNearby.setOnClickListener(f);

        View.OnClickListener t = v -> {
            changeTab((TextView) v);
            // 정렬/탭 로직: 여기에 Firestore 쿼리의 정렬 기준 (예: .orderBy("views", Query.Direction.DESCENDING))을 변경
            startListeningForPosts(); // 쿼리 변경 후 리스너 다시 시작 (새로운 정렬 적용)
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
    // 화면 아무 곳 터치 시 로그인 체크 메서드 (dispatchTouchEvent) 제거
    // 이 메서드는 앱 동작에 여러 문제를 일으킬 수 있으므로 제거했습니다.
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

        // 종목에 어울리는 제목 템플릿 목록
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

        for (int i = 0; i < 20; i++) {
            Post p = new Post();

            String selectedSport = sport[r.nextInt(sport.length)]; // 랜덤 종목 선택
            String titleTemplate = titleTemplates[r.nextInt(titleTemplates.length)]; // 랜덤 제목 템플릿 선택

            // 선택된 종목으로 제목 생성
            p.title    = String.format(titleTemplate, selectedSport);

            // 메타 내용도 종목에 맞게 조금 더 구체적으로 변경
            p.meta     = selectedSport + " 모임에 오신 것을 환영합니다! 상세 내용은 모임에 가입 후 확인해주세요.";

            p.location = selectedSport + " • 청주시 • 멤버 " + (5 + i) + "명";
            p.imageRes = R.drawable.placeholder_thumbnail; // 적절한 이미지 리소스로 변경
            // p.timestamp는 savePostToFirestore에서 FieldValue.serverTimestamp()로 설정되므로 여기서 필요 없습니다.
            // p.timestamp = new Date(System.currentTimeMillis() - (i * 1000 * 60)); // ✨ 이 줄은 제거합니다.
            savePostToFirestore(p); // 더미 데이터도 Firestore에 저장
        }
    }
}