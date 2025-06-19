package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View; // View import 추가
import android.widget.Button;
import android.widget.TextView; // TextViews for UI updates
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog; // AlertDialog 추가
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth; // Firebase Auth 추가
import com.google.firebase.auth.FirebaseUser; // Firebase User 추가
import android.util.Log;
import java.util.Objects; // Objects.requireNonNull을 위해 추가

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private Post currentPost; // 현재 모임 게시물 정보

    // UI 요소들
    private TextView tvDetailTitle;
    private TextView tvDetailMeta;
    private TextView tvDetailLocation;
    private TextView tvDetailContent;
    private Button chatWithHostButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail); // DetailActivity 레이아웃 설정

        // UI 요소 바인딩
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvDetailMeta = findViewById(R.id.tv_detail_meta);
        tvDetailLocation = findViewById(R.id.tv_detail_location);
        tvDetailContent = findViewById(R.id.tv_detail_content);
        chatWithHostButton = findViewById(R.id.chatWithHostButton);


        // Intent로부터 Post 객체 받기
        if (getIntent().hasExtra("post")) {
            currentPost = (Post) getIntent().getSerializableExtra("post"); // Post가 Serializable 구현해야 함
            if (currentPost != null) {
                // Post 객체 정보를 사용하여 UI 업데이트
                tvDetailTitle.setText(currentPost.getTitle());
                // tv_detail_meta에는 시간과 멤버 수가 들어갑니다.
                tvDetailMeta.setText(currentPost.getTime() + " | 멤버 " + currentPost.getCount() + "명");
                tvDetailLocation.setText(currentPost.getLocation());
                tvDetailContent.setText(currentPost.getContent());

                // "모임장과 채팅하기" 버튼 클릭 리스너 설정
                if (chatWithHostButton != null) {
                    chatWithHostButton.setOnClickListener(v -> {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                        Log.d(TAG, "Chat button clicked in DetailActivity.");
                        if (currentUser != null) {
                            Log.d(TAG, "Current Firebase user is logged in: " + currentUser.getUid() + " (" + currentUser.getEmail() + ")");
                        } else {
                            Log.d(TAG, "Current Firebase user is NULL. Showing login dialog.");
                        }

                        if (currentUser == null) {
                            // 로그인되어 있지 않다면 로그인 화면으로 유도
                            new AlertDialog.Builder(this)
                                    .setTitle("로그인이 필요합니다")
                                    .setMessage("채팅 기능을 이용하려면 로그인해야 합니다.")
                                    .setPositiveButton("로그인하기", (d, w) -> {
                                        startActivity(new Intent(this, LoginActivity.class));
                                    })
                                    .setNegativeButton("닫기", null)
                                    .show();
                            return;
                        }

                        // currentPost.getHostUid()는 모임장의 UID입니다.
                        // 이메일이나 DisplayName이 Firebase Auth에서 직접 가져오기 어렵다면,
                        // Post 객체에 호스트 이름(hostName) 필드를 추가하여 전달하는 것이 좋습니다.
                        // (Post.java에 hostName 필드가 이미 있다고 가정)

                        // 모임장 ID가 유효하고, 현재 사용자와 모임장이 다를 때만 채팅 시작
                        if (currentPost.getHostUid() != null && !currentPost.getHostUid().isEmpty()) { // Post.HostUid 사용
                            if (!currentPost.getHostUid().equals(currentUser.getUid())) { // 자기 자신과의 채팅 방지
                                Intent chatIntent = new Intent(DetailActivity.this, ChatActivity.class);

                                // 채팅방 ID 생성: 두 사용자 ID를 알파벳 순으로 결합하여 고유한 1:1 채팅방 ID 생성
                                String chatRoomId;
                                String userId1 = currentUser.getUid();
                                String userId2 = currentPost.getHostUid(); // 모임장 UID

                                if (userId1.compareTo(userId2) < 0) {
                                    chatRoomId = userId1 + "_" + userId2;
                                } else {
                                    chatRoomId = userId2 + "_" + userId1;
                                }

                                // ChatActivity에 채팅방 정보 전달
                                chatIntent.putExtra("chatRoomId", chatRoomId);
                                chatIntent.putExtra("otherUserId", currentPost.getHostUid());
                                chatIntent.putExtra("otherUserName", currentPost.getHostName()); // Post에 hostName 필드 필요
                                chatIntent.putExtra("postTitle", currentPost.getTitle()); // 채팅방 툴바 제목용

                                startActivity(chatIntent);
                            } else {
                                Toast.makeText(DetailActivity.this, "자신과의 채팅은 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DetailActivity.this, "모임장 정보를 찾을 수 없습니다. (UID 없음)", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(this, "모임 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                finish(); // 정보 없으면 액티비티 종료
            }
        } else {
            Toast.makeText(this, "모임 정보가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 정보 없으면 액티비티 종료
        }
    }
}