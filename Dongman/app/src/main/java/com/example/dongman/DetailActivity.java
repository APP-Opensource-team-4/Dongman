package com.example.dongman;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable; // Drawable import 유지
import android.net.Uri; // Uri import 유지
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.IOException; // IOException import 유지
import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private FirebaseFirestore db;
    private Post currentPost;

    private ImageView imgCover;
    private TextView tvDetailTitle, tvDetailMeta, tvDetailLocation, tvDetailContent;
    private Button chatWithHostButton;
    private Button btnJoin;
    private ImageButton btnFavorite;
    // private Button btnDetectPeople; // 자동 감지되므로 더 이상 필요 없을 수 있습니다.
    private TextView tvPeopleCount;

    private ObjectDetector objectDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        imgCover = findViewById(R.id.img_cover);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvDetailMeta = findViewById(R.id.tv_detail_meta);
        tvDetailLocation = findViewById(R.id.tv_detail_location);
        tvDetailContent = findViewById(R.id.tv_detail_content);
        chatWithHostButton = findViewById(R.id.chatWithHostButton);
        btnJoin = findViewById(R.id.btn_join);
        btnFavorite = findViewById(R.id.btn_favorite);
        // btnDetectPeople = findViewById(R.id.btn_detect_people); // 자동 감지되므로 주석 처리하거나 레이아웃에서 제거
        tvPeopleCount = findViewById(R.id.tv_people_count);

        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build();

        objectDetector = ObjectDetection.getClient(options);

        String postId = getIntent().getStringExtra("postId");
        if (postId != null) {
            loadPostData(postId);
        } else {
            Toast.makeText(this, "게시물 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // ---------- 기존 버튼 클릭 리스너 (변경된 부분 없음) ----------
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

        if (btnJoin != null) {
            btnJoin.setOnClickListener(v -> {
                Toast.makeText(DetailActivity.this, "모임 참여하기 클릭!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                Toast.makeText(DetailActivity.this, "즐겨찾기 클릭!", Toast.LENGTH_SHORT).show();
            });
        }

        // '사진에서 사람 수 감지하기' 버튼 클릭 리스너 제거 또는 주석 처리
        // if (btnDetectPeople != null) {
        //     btnDetectPeople.setOnClickListener(v -> {
        //         if (currentPost != null && currentPost.getFirstImageUrl() != null) {
        //             detectPeopleInImage(currentPost.getFirstImageUrl());
        //         } else {
        //             Toast.makeText(this, "감지할 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
        //         }
        //     });
        // }
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
                        String imageUrl = currentPost.getFirstImageUrl();
                        Glide.with(this).load(imageUrl).into(imgCover);

                        // 이미지 로드 후 자동으로 객체 감지 시작
                        detectPeopleInImage(imageUrl);
                    } else {
                        imgCover.setImageResource(R.drawable.camera_logo);
                        // 이미지가 없는 경우 감지할 객체가 없음을 표시
                        tvPeopleCount.setText("이미지가 없습니다. 객체 감지 불가.");
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

    // ML Kit을 사용하여 이미지에서 사람 감지
    private void detectPeopleInImage(String imageUrl) {
        // Glide를 사용하여 URL에서 비트맵을 로드
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        // 비트맵 로드 성공! 이제 ML Kit으로 처리
                        InputImage image = InputImage.fromBitmap(bitmap, 0); // 회전 각도 0으로 가정

                        objectDetector.process(image)
                                .addOnSuccessListener(detectedObjects -> {
                                    int peopleCount = 0;
                                    Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                    Canvas canvas = new Canvas(mutableBitmap);
                                    Paint paint = new Paint();
                                    paint.setColor(Color.RED);
                                    paint.setStyle(Paint.Style.STROKE);
                                    paint.setStrokeWidth(5);
                                    paint.setTextSize(40);
                                    paint.setColor(Color.BLUE); // 텍스트 색상

                                    for (DetectedObject detectedObject : detectedObjects) {
                                        // ML Kit의 ObjectDetection 기본 모델은 'person' 레이블을 직접 제공하지 않을 수 있습니다.
                                        // '사람'으로 정확히 분류하려면 Face Detection을 사용하거나 커스텀 모델이 필요합니다.
                                        // 여기서는 모든 감지된 객체에 바운딩 박스를 그립니다.
                                        canvas.drawRect(detectedObject.getBoundingBox(), paint);
                                        peopleCount++; // 감지된 객체 수를 셉니다.

                                        // 라벨이 있다면 라벨도 그립니다.
                                        if (detectedObject.getLabels() != null && !detectedObject.getLabels().isEmpty()) {
                                            String label = detectedObject.getLabels().get(0).getText();
                                            canvas.drawText(label,
                                                    detectedObject.getBoundingBox().left,
                                                    detectedObject.getBoundingBox().top - 10,
                                                    paint);
                                        }
                                    }
                                    imgCover.setImageBitmap(mutableBitmap); // 결과 이미지를 ImageView에 표시

                                    tvPeopleCount.setText("감지된 객체 수 (바운딩 박스 기준): " + peopleCount + "명");

                                    if (currentPost != null) {
                                        int totalMembers = currentPost.getCount();
                                        if (totalMembers > 0) {
                                            double participationRate = (double) peopleCount / totalMembers * 100;
                                            tvPeopleCount.append("\n모집 인원: " + totalMembers + "명");
                                            tvPeopleCount.append(String.format("\n참여율: %.2f%%", participationRate));
                                        } else {
                                            tvPeopleCount.append("\n모집 인원이 0명입니다. 참여율을 계산할 수 없습니다.");
                                        }
                                    }

                                    Toast.makeText(DetailActivity.this, "감지된 객체 수: " + peopleCount + "명", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DetailActivity.this, "객체 감지 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Object detection failed: " + e.getMessage(), e);
                                });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // 이미지 로드가 취소되거나 실패했을 때 처리
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) { // Corrected method name and parameter
                        // 이미지 로드 실패 시
                        Toast.makeText(DetailActivity.this, "이미지를 불러올 수 없습니다. (Glide 실패)", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Glide image loading failed: " + (errorDrawable != null ? "Drawable: " + errorDrawable.toString() : "Unknown error"));
                        // 이미지 로드 실패 시 tvPeopleCount에 오류 메시지 표시
                        tvPeopleCount.setText("이미지 로드 실패로 감지 불가.");
                    }
                });
    }
}