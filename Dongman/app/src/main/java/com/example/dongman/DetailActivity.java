package com.example.dongman;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private FirebaseFirestore db;
    private Post currentPost;

    // UI elements - 모든 버튼을 View로 선언하여 캐스팅 오류 방지
    private ImageView imgCover;
    private TextView tvDetailTitle;
    private TextView tvDetailMeta;
    // private TextView tvDetailLocation; // XML에 없으므로 제거
    private TextView tvDetailContent;
    private View chatWithHostButton;  // Button 대신 View 사용
    private View btnJoin;             // Button 대신 View 사용
    private View btnFavorite;         // AppCompatImageButton 대신 View 사용
    private View btnMap;              // Button 대신 View 사용

    private static final String PREF_KEY_RECENT = "recent_posts";


    private void safeLaunch(Class<?> c) {
        if (LoginHelper.isLoggedIn(this)) {
            if (c.equals(PostWriteActivity.class)) {
                writeLauncher.launch(new Intent(this, c));
            } else {
                startActivity(new Intent(this, c));
            }
        } else {
            showLoginDialog();
        }
    }

    private final ActivityResultLauncher<Intent> writeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(this, "게시물이 성공적으로 작성되었습니다!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "PostWriteActivity completed successfully.");
                }
            });

    private void showLoginDialog() {
        new AlertDialog.Builder(this)
                .setTitle("로그인이 필요합니다")
                .setMessage("해당 기능은 로그인 후 이용할 수 있습니다.")
                .setPositiveButton("로그인하기", (d, w) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .setNegativeButton("닫기", null)
                .show();
    }

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

        // UI binding with null checks
        imgCover = findViewById(R.id.img_cover);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvDetailMeta = findViewById(R.id.tv_detail_meta);
        // tvDetailLocation = findViewById(R.id.tv_detail_location); // XML에 없으므로 제거
        tvDetailContent = findViewById(R.id.tv_detail_content);
        chatWithHostButton = findViewById(R.id.chatWithHostButton);
        btnJoin = findViewById(R.id.btn_join);
//        btnFavorite = findViewById(R.id.btn_favorite);
        btnMap = findViewById(R.id.btn_map);

        // Null check for critical views
        if (tvDetailTitle == null) Log.e(TAG, "tvDetailTitle is null - check R.id.tv_detail_title");
        if (tvDetailMeta == null) Log.e(TAG, "tvDetailMeta is null - check R.id.tv_detail_meta");
        // if (tvDetailLocation == null) Log.e(TAG, "tvDetailLocation is null - check R.id.tv_detail_location"); // 제거
        if (tvDetailContent == null) Log.e(TAG, "tvDetailContent is null - check R.id.tv_detail_content");

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
        chatWithHostButton.setOnClickListener(v -> safeLaunch(ChatActivity.class));

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

    private void startActivity(Class<ChatActivity> chatActivityClass) {
    }

    private void loadPostData(String postId) {
        DocumentReference docRef = db.collection("posts").document(postId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentPost = documentSnapshot.toObject(Post.class);
                if (currentPost != null) {
                    currentPost.setId(documentSnapshot.getId());

                    // Set text with null checks
                    if (tvDetailTitle != null) {
                        tvDetailTitle.setText(currentPost.getTitle());
                    }
                    if (tvDetailMeta != null) {
                        tvDetailMeta.setText(currentPost.getTime() + " | " + currentPost.getLocation());
                    }
                    // location은 meta에 포함시킴 (별도 TextView가 없으므로)
                    if (tvDetailContent != null) {
                        tvDetailContent.setText(currentPost.getContent());
                    }
                    Objects.requireNonNull(getSupportActionBar()).setTitle(currentPost.getTitle());

                    // 이미지 로딩 로그 추가
                    if (currentPost.getImageUrls() != null && !currentPost.getImageUrls().isEmpty()) {
                        String imageUrl = currentPost.getImageUrls().get(0);
                        Log.d(TAG, "Loading image from URL: " + imageUrl);
                        Log.d(TAG, "Total images: " + currentPost.getImageUrls().size());

                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_thumbnail)
                                .error(R.drawable.camera_logo)
                                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                        Log.e(TAG, "Image load failed: " + e.getMessage());
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                        Log.d(TAG, "Image loaded successfully from: " + dataSource);
                                        return false;
                                    }
                                })
                                .into(imgCover);
                    } else {
                        Log.w(TAG, "No image URLs found, using default image");
                        imgCover.setImageResource(R.drawable.camera_logo);
                    }

                    saveRecentPost(currentPost);
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

    public static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

        private List<Message> messageList;

        public ChatAdapter(List<Message> messageList) {
            this.messageList = messageList;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            Message message = messageList.get(position);

            // 메시지 내용 설정
            holder.tvMessageContent.setText(message.getContent());

            // 시간 포맷팅
            String timeString = formatTime(message.getTimestamp());

            if (message.getType() == Message.Type.RIGHT) {
                // 내 메시지 (오른쪽)
                setupMyMessage(holder, timeString);
            } else {
                // 상대방 메시지 (왼쪽)
                setupOtherMessage(holder, message, timeString);
            }
        }

        private void setupMyMessage(ChatViewHolder holder, String timeString) {
            // 메시지 정렬을 오른쪽으로
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                    holder.layoutMessageContent.getLayoutParams();
            params.gravity = Gravity.END;
            holder.layoutMessageContent.setLayoutParams(params);

            // 내 메시지 스타일 - 더 안전한 색상 사용
            holder.cardMessage.setCardBackgroundColor(0xFF007AFF); // 파란색
            holder.tvMessageContent.setTextColor(0xFFFFFFFF); // 흰색

            // 시간 표시 (메시지 왼쪽)
            if (holder.tvTimeRight != null) {
                holder.tvTimeRight.setText(timeString);
                holder.tvTimeRight.setVisibility(View.VISIBLE);
            }
            if (holder.tvTimeLeft != null) {
                holder.tvTimeLeft.setVisibility(View.GONE);
            }

            // 프로필 이미지 숨기기
            if (holder.imgProfileOther != null) {
                holder.imgProfileOther.setVisibility(View.GONE);
            }
            if (holder.imgProfileMine != null) {
                holder.imgProfileMine.setVisibility(View.GONE);
            }

            // 이름 숨기기
            if (holder.tvSenderName != null) {
                holder.tvSenderName.setVisibility(View.GONE);
            }
        }

        private void setupOtherMessage(ChatViewHolder holder, Message message, String timeString) {
            // 메시지 정렬을 왼쪽으로
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                    holder.layoutMessageContent.getLayoutParams();
            params.gravity = Gravity.START;
            holder.layoutMessageContent.setLayoutParams(params);

            // 상대방 메시지 스타일
            holder.cardMessage.setCardBackgroundColor(0xFFFFFFFF); // 흰색
            holder.tvMessageContent.setTextColor(0xFF000000); // 검은색

            // 시간 표시 (메시지 오른쪽)
            if (holder.tvTimeLeft != null) {
                holder.tvTimeLeft.setText(timeString);
                holder.tvTimeLeft.setVisibility(View.VISIBLE);
            }
            if (holder.tvTimeRight != null) {
                holder.tvTimeRight.setVisibility(View.GONE);
            }

            // 프로필 이미지 표시
            if (holder.imgProfileOther != null) {
                holder.imgProfileOther.setVisibility(View.VISIBLE);
            }
            if (holder.imgProfileMine != null) {
                holder.imgProfileMine.setVisibility(View.GONE);
            }

            // 발신자 이름 표시
            if (holder.tvSenderName != null) {
                holder.tvSenderName.setText(message.getSenderName());
                holder.tvSenderName.setVisibility(View.VISIBLE);
            }
        }

        private String formatTime(Date timestamp) {
            if (timestamp == null) {
                return "";
            }

            SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.KOREA);
            return sdf.format(timestamp);
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }

        static class ChatViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layoutMessageContainer;
            LinearLayout layoutMessageContent;
            CardView cardMessage;
            TextView tvMessageContent;
            TextView tvSenderName;
            TextView tvTimeLeft;
            TextView tvTimeRight;
            ImageView imgProfileOther;
            ImageView imgProfileMine;

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                // 안전한 findViewById - null 체크 없이
                layoutMessageContainer = itemView.findViewById(R.id.layout_message_container);
                layoutMessageContent = itemView.findViewById(R.id.layout_message_content);
                cardMessage = itemView.findViewById(R.id.card_message);
                tvMessageContent = itemView.findViewById(R.id.tv_message_content);
                tvSenderName = itemView.findViewById(R.id.tv_sender_name);
                tvTimeLeft = itemView.findViewById(R.id.tv_time_left);
                tvTimeRight = itemView.findViewById(R.id.tv_time_right);
                imgProfileOther = itemView.findViewById(R.id.img_profile_other);
                imgProfileMine = itemView.findViewById(R.id.img_profile_mine);
            }
        }
    }

    private void saveRecentPost(Post post) {
        if (post == null) return;

        SharedPreferences sp = getSharedPreferences(PREF_KEY_RECENT, MODE_PRIVATE);
        String jsonList = sp.getString("list", null);

        Type t = new com.google.gson.reflect.TypeToken<ArrayList<Post>>(){}.getType();
        List<Post> list = (jsonList == null) ? new ArrayList<>() :
                new com.google.gson.Gson().fromJson(jsonList, t);

        /* 중복 제거: 같은 id 있으면 삭제 */
        for (int i = 0; i < list.size(); i++) {
            if (post.getId().equals(list.get(i).getId())) {
                list.remove(i); break;
            }
        }
        list.add(post);                         // 맨 끝에(최신) 추가
        if (list.size() > 30) list.remove(0);   // 보관 개수 제한(예: 30개)

        sp.edit().putString("list",
                new com.google.gson.Gson().toJson(list)).apply();
    }

}