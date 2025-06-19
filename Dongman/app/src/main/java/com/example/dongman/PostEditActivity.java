package com.example.dongman;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostEditActivity extends AppCompatActivity {

    private static final String TAG = "PostEditActivity";
    private static final int MAX_IMAGES = 5;

    // UI 요소
    private EditText titleEditText;
    private EditText contentEditText;
    private EditText maxParticipantsEditText;
    private Button dateButton;
    private Button timeButton;
    private Button locationButton;
    private Button addImageButton;
    private Button saveButton;
    private Button deleteButton;
    private RecyclerView imageRecyclerView;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    // 데이터
    private String postId;
    private Post currentPost;
    private List<String> imageUrls = new ArrayList<>();
    private List<Uri> newImageUris = new ArrayList<>();
    private ImageEditAdapter imageAdapter;

    // 날짜 및 시간
    private Calendar selectedDateTime = Calendar.getInstance();
    private String selectedLocation = "";

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> locationPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit);

        // Intent에서 postId 받기
        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "잘못된 접근입니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // UI 초기화
        initializeUI();

        // Activity Result Launchers 초기화
        initializeActivityLaunchers();

        // 기존 게시물 데이터 로드
        loadPostData();
    }

    private void initializeUI() {
        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("모임 수정");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // UI 요소 초기화
        titleEditText = findViewById(R.id.edit_title);
        contentEditText = findViewById(R.id.edit_content);
        maxParticipantsEditText = findViewById(R.id.edit_max_participants);
        dateButton = findViewById(R.id.btn_select_date);
        timeButton = findViewById(R.id.btn_select_time);
        locationButton = findViewById(R.id.btn_select_location);
        addImageButton = findViewById(R.id.btn_add_image);
        saveButton = findViewById(R.id.btn_save);
        deleteButton = findViewById(R.id.btn_delete);
        imageRecyclerView = findViewById(R.id.recycler_images);
        progressBar = findViewById(R.id.progress_bar);

        // 이미지 RecyclerView 설정
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageEditAdapter(imageUrls, this::removeImage);
        imageRecyclerView.setAdapter(imageAdapter);

        // 버튼 클릭 리스너
        dateButton.setOnClickListener(v -> showDatePicker());
        timeButton.setOnClickListener(v -> showTimePicker());
        locationButton.setOnClickListener(v -> openLocationPicker());
        addImageButton.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> savePost());
        deleteButton.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void initializeActivityLaunchers() {
        // 이미지 선택 런처
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            // 여러 이미지 선택
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count && imageUrls.size() + newImageUris.size() < MAX_IMAGES; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                newImageUris.add(imageUri);
                            }
                        } else if (data.getData() != null) {
                            // 단일 이미지 선택
                            if (imageUrls.size() + newImageUris.size() < MAX_IMAGES) {
                                newImageUris.add(data.getData());
                            }
                        }
                        updateImageAdapter();
                    }
                }
        );

        // 위치 선택 런처 (MapActivity 사용)
        locationPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedLocation = result.getData().getStringExtra("location");
                        locationButton.setText(selectedLocation);
                    }
                }
        );
    }

    private void loadPostData() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("posts").document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentPost = documentSnapshot.toObject(Post.class);
                        if (currentPost != null) {
                            currentPost.setId(documentSnapshot.getId());
                            populateUI();
                        }
                    } else {
                        Toast.makeText(this, "게시물을 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load post", e);
                    Toast.makeText(this, "게시물 로드 실패", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                });
    }

    private void populateUI() {
        titleEditText.setText(currentPost.getTitle());
        contentEditText.setText(currentPost.getContent());
        maxParticipantsEditText.setText(String.valueOf(currentPost.getMaxParticipants()));

        // 날짜 및 시간 설정
        if (currentPost.getDateTime() != null) {
            selectedDateTime.setTime(currentPost.getDateTime());
            updateDateTimeButtons();
        }

        // 위치 설정
        if (currentPost.getLocation() != null && !currentPost.getLocation().isEmpty()) {
            selectedLocation = currentPost.getLocation();
            locationButton.setText(selectedLocation);
        }

        // 이미지 설정
        if (currentPost.getImageUrls() != null) {
            imageUrls.clear();
            imageUrls.addAll(currentPost.getImageUrls());
            imageAdapter.notifyDataSetChanged();
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeButtons();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeButtons();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDateTimeButtons() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        dateButton.setText(dateFormat.format(selectedDateTime.getTime()));
        timeButton.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void openLocationPicker() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("mode", "select");
        if (!selectedLocation.isEmpty()) {
            intent.putExtra("currentLocation", selectedLocation);
        }
        locationPickerLauncher.launch(intent);
    }

    private void openImagePicker() {
        if (imageUrls.size() + newImageUris.size() >= MAX_IMAGES) {
            Toast.makeText(this, "최대 " + MAX_IMAGES + "장까지 선택할 수 있습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    private void updateImageAdapter() {
        // 기존 이미지 URL과 새 이미지 URI를 합쳐서 표시
        List<String> allImages = new ArrayList<>(imageUrls);
        for (Uri uri : newImageUris) {
            allImages.add(uri.toString());
        }
        imageAdapter.updateImages(allImages);
    }

    private void removeImage(int position) {
        if (position < imageUrls.size()) {
            // 기존 이미지 제거
            imageUrls.remove(position);
        } else {
            // 새 이미지 제거
            int newImageIndex = position - imageUrls.size();
            if (newImageIndex < newImageUris.size()) {
                newImageUris.remove(newImageIndex);
            }
        }
        updateImageAdapter();
    }

    private void savePost() {
        // 입력 값 검증
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String maxParticipantsStr = maxParticipantsEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("제목을 입력하세요");
            return;
        }

        if (content.isEmpty()) {
            contentEditText.setError("내용을 입력하세요");
            return;
        }

        if (maxParticipantsStr.isEmpty()) {
            maxParticipantsEditText.setError("최대 참가자 수를 입력하세요");
            return;
        }

        int maxParticipants;
        try {
            maxParticipants = Integer.parseInt(maxParticipantsStr);
            if (maxParticipants <= 0) {
                maxParticipantsEditText.setError("1 이상의 숫자를 입력하세요");
                return;
            }
        } catch (NumberFormatException e) {
            maxParticipantsEditText.setError("올바른 숫자를 입력하세요");
            return;
        }

        if (selectedLocation.isEmpty()) {
            Toast.makeText(this, "위치를 선택하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // 새 이미지 업로드 후 게시물 업데이트
        if (!newImageUris.isEmpty()) {
            uploadNewImages(() -> updatePostInFirestore(title, content, maxParticipants));
        } else {
            updatePostInFirestore(title, content, maxParticipants);
        }
    }

    private void uploadNewImages(Runnable onComplete) {
        List<String> newImageUrls = new ArrayList<>();
        int[] uploadCount = {0};

        for (Uri imageUri : newImageUris) {
            String fileName = "post_images/" + System.currentTimeMillis() + "_" + uploadCount[0] + ".jpg";
            StorageReference imageRef = storage.getReference().child(fileName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            newImageUrls.add(uri.toString());
                            uploadCount[0]++;

                            if (uploadCount[0] == newImageUris.size()) {
                                imageUrls.addAll(newImageUrls);
                                onComplete.run();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to upload image", e);
                        uploadCount[0]++;
                        if (uploadCount[0] == newImageUris.size()) {
                            imageUrls.addAll(newImageUrls);
                            onComplete.run();
                        }
                    });
        }
    }

    private void updatePostInFirestore(String title, String content, int maxParticipants) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("content", content);
        updates.put("maxParticipants", maxParticipants);
        updates.put("dateTime", selectedDateTime.getTime());
        updates.put("location", selectedLocation);
        updates.put("imageUrls", imageUrls);
        updates.put("updatedAt", new Date());

        db.collection("posts").document(postId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "모임이 수정되었습니다", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update post", e);
                    Toast.makeText(this, "수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                });
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("모임 삭제")
                .setMessage("정말로 이 모임을 삭제하시겠습니까?\n삭제된 모임은 복구할 수 없습니다.")
                .setPositiveButton("삭제", (dialog, which) -> deletePost())
                .setNegativeButton("취소", null)
                .show();
    }

    private void deletePost() {
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);
        deleteButton.setEnabled(false);

        // Firestore에서 게시물 삭제
        db.collection("posts").document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Storage에서 이미지들 삭제
                    deletePostImages();

                    Toast.makeText(this, "모임이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Post deleted successfully");

                    // 결과를 MyPostsActivity에 전달하고 종료
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete post", e);
                    Toast.makeText(this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                });
    }

    private void deletePostImages() {
        if (currentPost != null && currentPost.getImageUrls() != null && !currentPost.getImageUrls().isEmpty()) {
            for (String imageUrl : currentPost.getImageUrls()) {
                try {
                    StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
                    imageRef.delete()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Image deleted: " + imageUrl))
                            .addOnFailureListener(e -> Log.w(TAG, "Failed to delete image: " + imageUrl, e));
                } catch (Exception e) {
                    Log.w(TAG, "Invalid image URL: " + imageUrl, e);
                }
            }
        }
    }
}