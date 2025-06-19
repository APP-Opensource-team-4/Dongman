// DetailActivity.java
package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton; // Added for btnFavorite
import android.widget.ImageView; // Added for imgCover
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Added for Toolbar

import com.bumptech.glide.Glide; // Added for Glide
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference; // Added for Firestore
import com.google.firebase.firestore.FirebaseFirestore; // Added for Firestore

import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private FirebaseFirestore db; // Firestore instance
    private Post currentPost; // Current meeting post information

    // UI elements
    private ImageView imgCover; // For post image
    private TextView tvDetailTitle;
    private TextView tvDetailMeta;
    private TextView tvDetailLocation;
    private TextView tvDetailContent;
    private Button chatWithHostButton;
    private Button btnJoin; // Join button
    private ImageButton btnFavorite; // Favorite button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail); // DetailActivity layout settings

        // Firebase initialization
        db = FirebaseFirestore.getInstance();

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // UI element binding
        imgCover = findViewById(R.id.img_cover);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvDetailMeta = findViewById(R.id.tv_detail_meta);
        tvDetailLocation = findViewById(R.id.tv_detail_location);
        tvDetailContent = findViewById(R.id.tv_detail_content);
        chatWithHostButton = findViewById(R.id.chatWithHostButton);
        btnJoin = findViewById(R.id.btn_join);
        btnFavorite = findViewById(R.id.btn_favorite);

        // Get postId from Intent
        String postId = getIntent().getStringExtra("postId");
        if (postId != null) {
            loadPostData(postId);
        } else {
            Toast.makeText(this, "게시물 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish(); // Finish activity if no post information is passed
        }

        // "Chat with Host" button click listener setup
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
                    // If not logged in, prompt to log in
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

                // currentPost.getHostUid() is the host's UID.
                // If email or DisplayName are hard to get directly from Firebase Auth,
                // it's good to add a host name (hostName) field to the Post object.
                // (Assuming Post.java already has a hostName field)

                // Start chat only if host ID is valid and current user is different from host
                if (currentPost.getHostUid() != null && !currentPost.getHostUid().isEmpty()) {
                    if (!currentPost.getHostUid().equals(currentUser.getUid())) { // Prevent chatting with self
                        Intent chatIntent = new Intent(DetailActivity.this, ChatActivity.class);

                        // Generate chat room ID: Combine two user IDs alphabetically to create a unique 1:1 chat room ID
                        String chatRoomId;
                        String userId1 = currentUser.getUid();
                        String userId2 = currentPost.getHostUid(); // Host UID

                        if (userId1.compareTo(userId2) < 0) {
                            chatRoomId = userId1 + "_" + userId2;
                        } else {
                            chatRoomId = userId2 + "_" + userId1;
                        }

                        // Pass chat room information to ChatActivity
                        chatIntent.putExtra("chatRoomId", chatRoomId);
                        chatIntent.putExtra("otherUserId", currentPost.getHostUid());
                        chatIntent.putExtra("otherUserName", currentPost.getHostName()); // Post needs hostName field
                        chatIntent.putExtra("postTitle", currentPost.getTitle()); // For chat room toolbar title

                        startActivity(chatIntent);
                    } else {
                        Toast.makeText(DetailActivity.this, "자신과의 채팅은 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetailActivity.this, "모임장 정보를 찾을 수 없습니다. (UID 없음)", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Example listeners for other buttons (Join, Favorite) - implement actual logic as needed
        if (btnJoin != null) {
            btnJoin.setOnClickListener(v -> {
                // Handle join button click
                Toast.makeText(DetailActivity.this, "모임 참여하기 클릭!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                // Handle favorite button click
                Toast.makeText(DetailActivity.this, "즐겨찾기 클릭!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadPostData(String postId) {
        DocumentReference docRef = db.collection("posts").document(postId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentPost = documentSnapshot.toObject(Post.class); // Assign to currentPost
                if (currentPost != null) {
                    currentPost.setId(documentSnapshot.getId()); // Set the ID

                    // Update UI with Post object information
                    tvDetailTitle.setText(currentPost.getTitle());
                    tvDetailMeta.setText(currentPost.getTime() + " | 멤버 " + currentPost.getCount() + "명");
                    tvDetailLocation.setText(currentPost.getLocation());
                    tvDetailContent.setText(currentPost.getContent());
                    Objects.requireNonNull(getSupportActionBar()).setTitle(currentPost.getTitle()); // Set toolbar title

                    // Load image using Glide
                    if (currentPost.getImageUrls() != null && !currentPost.getImageUrls().isEmpty()) {
                        Glide.with(this).load(currentPost.getFirstImageUrl()).into(imgCover);
                    } else {
                        imgCover.setImageResource(R.drawable.camera_logo); // Default image if none
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