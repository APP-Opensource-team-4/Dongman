package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private FirebaseFirestore db;
    private Post currentPost;

    // UI elements
    private ImageView imgCover;
    private TextView tvDetailTitle;
    private TextView tvDetailMeta;
    private TextView tvDetailLocation;
    private TextView tvDetailContent;
    private Button chatWithHostButton;
    private Button btnJoin;
    private ImageButton btnFavorite;

    // ✅ 추가된 지도 버튼
    private Button btnMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = FirebaseFirestore.getInstance();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // UI binding
        imgCover = findViewById(R.id.img_cover);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvDetailMeta = findViewById(R.id.tv_detail_meta);
        tvDetailContent = findViewById(R.id.tv_detail_content);
        chatWithHostButton = findViewById(R.id.chatWithHostButton);
        btnJoin = findViewById(R.id.btn_join);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnMap = findViewById(R.id.btn_map); // ✅ 지도 보기 버튼 바인딩

        // 게시물 ID 받기
        String postId = getIntent().getStringExtra("postId");
        if (postId != null) {
            loadPostData(postId);
        } else {
            Toast.makeText(this, "게시물 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // ✅ 지도 보기 버튼 클릭
        if (btnMap != null) {
            btnMap.setOnClickListener(v -> {
                if (currentPost != null && currentPost.getLocation() != null) {
                    Intent intent = new Intent(DetailActivity.this, MapActivity.class);
                    intent.putExtra("location_name", currentPost.getLocation());
                    startActivity(intent);
                } else {
                    Toast.makeText(DetailActivity.this, "모임 장소 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 채팅 버튼
        if (chatWithHostButton != null) {
            chatWithHostButton.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser == null) {
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

                if (currentPost == null) {
                    Toast.makeText(DetailActivity.this, "모임 정보가 아직 로드되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentPost.getHostUid() != null && !currentPost.getHostUid().isEmpty()) {
                    if (!currentPost.getHostUid().equals(currentUser.getUid())) {
                        Intent chatIntent = new Intent(DetailActivity.this, ChatActivity.class);

                        String chatRoomId;
                        String userId1 = currentUser.getUid();
                        String userId2 = currentPost.getHostUid();

                        if (userId1.compareTo(userId2) < 0) {
                            chatRoomId = userId1 + "_" + userId2;
                        } else {
                            chatRoomId = userId2 + "_" + userId1;
                        }

                        chatIntent.putExtra("chatRoomId", chatRoomId);
                        chatIntent.putExtra("otherUserId", currentPost.getHostUid());
                        chatIntent.putExtra("otherUserName", currentPost.getHostName());
                        chatIntent.putExtra("postTitle", currentPost.getTitle());

                        startActivity(chatIntent);
                    } else {
                        Toast.makeText(DetailActivity.this, "자신과의 채팅은 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetailActivity.this, "모임장 정보를 찾을 수 없습니다. (UID 없음)", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 모임 참여 버튼
        if (btnJoin != null) {
            btnJoin.setOnClickListener(v -> {
                Toast.makeText(DetailActivity.this, "모임 참여하기 클릭!", Toast.LENGTH_SHORT).show();
            });
        }

        // 즐겨찾기 버튼
        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                Toast.makeText(DetailActivity.this, "즐겨찾기 클릭!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadPostData(String postId) {
        DocumentReference docRef = db.collection("posts").document(postId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentPost = documentSnapshot.toObject(Post.class);
                if (currentPost != null) {
                    currentPost.setId(documentSnapshot.getId());

                    tvDetailTitle.setText(currentPost.getTitle());
                    tvDetailMeta.setText(currentPost.getTime() + " | 멤버 " + currentPost.getCount() + "명");
                    tvDetailLocation.setText(currentPost.getLocation());
                    tvDetailContent.setText(currentPost.getContent());
                    Objects.requireNonNull(getSupportActionBar()).setTitle(currentPost.getTitle());

                    if (currentPost.getImageUrls() != null && !currentPost.getImageUrls().isEmpty()) {
                        Glide.with(this).load(currentPost.getFirstImageUrl()).into(imgCover);
                    } else {
                        imgCover.setImageResource(R.drawable.camera_logo);
                    }
                }
            } else {
                Toast.makeText(this, "해당 게시물이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "게시물 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error loading post: " + e.getMessage(), e);
            finish();
        });
    }
}
