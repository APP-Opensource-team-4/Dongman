package com.example.dongman;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostWriteActivity extends AppCompatActivity {

    private static final String TAG = "PostWriteActivity";
    private static final int MAX_PHOTOS = 5;

    // UI 요소
    private EditText etTitle, etCount, etLocation, etIntro;
    private Spinner spinnerTime;
    private Button btnSubmit;
    private ImageButton btnAddPhoto;
    private TextView tvPhotoCount;
    private LinearLayout layoutImagePreviews;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;

    // 이미지 관련
    private List<Uri> selectedImageUris = new ArrayList<>();
    private List<String> uploadedImageUrls = new ArrayList<>();

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Firebase 초기화
        initializeFirebase();

        // UI 초기화
        initializeUI();

        // Activity Result Launchers 초기화
        initializeLaunchers();

        // 로그인 상태 확인
        checkLoginStatus();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void initializeUI() {
        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // UI 요소 찾기
        etTitle = findViewById(R.id.et_title);
        spinnerTime = findViewById(R.id.spinner_time);
        etCount = findViewById(R.id.et_count);
        etLocation = findViewById(R.id.et_location);
        etIntro = findViewById(R.id.et_intro);
        btnSubmit = findViewById(R.id.btn_submit);
        btnAddPhoto = findViewById(R.id.btn_add_photo);
        tvPhotoCount = findViewById(R.id.tv_photo_count);
        layoutImagePreviews = findViewById(R.id.layout_image_previews);
        progressBar = findViewById(R.id.progress_bar);

        // 시간 스피너 설정
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_options, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(timeAdapter);

        // 버튼 리스너 설정
        btnAddPhoto.setOnClickListener(v -> checkPermissionAndOpenGallery());
        btnSubmit.setOnClickListener(v -> handleSubmit());

        // 초기 상태 설정
        updatePhotoCount();
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void initializeLaunchers() {
        // 이미지 선택 런처
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        handleImageSelection(result.getData());
                    }
                }
        );

        // 권한 요청 런처
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "사진 접근 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkLoginStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d(TAG, "=== 로그인 상태 확인 ===");
        Log.d(TAG, "Firebase User: " + currentUser);
        if (currentUser != null) {
            Log.d(TAG, "User UID: " + currentUser.getUid());
            Log.d(TAG, "User Email: " + currentUser.getEmail());
        } else {
            Log.d(TAG, "로그인되지 않음");
        }
    }

    private void checkPermissionAndOpenGallery() {
        if (selectedImageUris.size() >= MAX_PHOTOS) {
            Toast.makeText(this, "최대 " + MAX_PHOTOS + "장까지만 선택할 수 있습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "이미지 선택"));
    }

    private void handleImageSelection(Intent data) {
        if (data.getClipData() != null) {
            // 다중 이미지 선택
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count && selectedImageUris.size() < MAX_PHOTOS; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                selectedImageUris.add(imageUri);
            }
        } else if (data.getData() != null) {
            // 단일 이미지 선택
            if (selectedImageUris.size() < MAX_PHOTOS) {
                selectedImageUris.add(data.getData());
            }
        }

        if (selectedImageUris.size() >= MAX_PHOTOS) {
            Toast.makeText(this, "최대 " + MAX_PHOTOS + "장까지만 선택할 수 있습니다", Toast.LENGTH_SHORT).show();
        }

        updateImagePreviews();
        updatePhotoCount();
    }

    private void updatePhotoCount() {
        tvPhotoCount.setText(selectedImageUris.size() + "/" + MAX_PHOTOS);
    }

    private void updateImagePreviews() {
        layoutImagePreviews.removeAllViews();

        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri uri = selectedImageUris.get(i);
            addImagePreview(uri, i);
        }
    }

    private void addImagePreview(Uri uri, int index) {
        ConstraintLayout container = new ConstraintLayout(this);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(200, 200);
        containerParams.setMargins(0, 0, 16, 0);
        container.setLayoutParams(containerParams);

        // 이미지 뷰
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(uri).into(imageView);
        container.addView(imageView);

        // 삭제 버튼
        ImageButton deleteButton = new ImageButton(this);
        ConstraintLayout.LayoutParams deleteParams = new ConstraintLayout.LayoutParams(60, 60);
        deleteParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        deleteParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        deleteButton.setLayoutParams(deleteParams);
        deleteButton.setBackgroundResource(android.R.color.transparent);
        deleteButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        deleteButton.setOnClickListener(v -> removeImage(index));
        container.addView(deleteButton);

        layoutImagePreviews.addView(container);
    }

    private void removeImage(int index) {
        if (index >= 0 && index < selectedImageUris.size()) {
            selectedImageUris.remove(index);
            updateImagePreviews();
            updatePhotoCount();
        }
    }

    private void handleSubmit() {
        // 입력 값 검증
        String title = etTitle.getText().toString().trim();
        String time = spinnerTime.getSelectedItem().toString();
        String countStr = etCount.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String content = etIntro.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("제목을 입력하세요");
            etTitle.requestFocus();
            return;
        }

        if (countStr.isEmpty()) {
            etCount.setError("모집 인원을 입력하세요");
            etCount.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            etLocation.setError("위치를 입력하세요");
            etLocation.requestFocus();
            return;
        }

        if (content.isEmpty()) {
            etIntro.setError("모임 소개를 입력하세요");
            etIntro.requestFocus();
            return;
        }

        int count;
        try {
            count = Integer.parseInt(countStr);
            if (count <= 0) {
                etCount.setError("1 이상의 숫자를 입력하세요");
                etCount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etCount.setError("올바른 숫자를 입력하세요");
            etCount.requestFocus();
            return;
        }

        // 게시물 업로드 시작 (원래 방식 사용)
        uploadPost(title, time, count, location, content);
    }

    private void uploadPost(String title, String time, int count, String location, String content) {
        // 원래 코드의 로그인 체크 방식 사용
        if (!LoginHelper.isLoggedIn(this)) {
            Toast.makeText(this, "게시물 작성에는 로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            return;
        }

        // Firebase User 정보 가져오기 (없어도 진행)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String hostUid = getUserId(currentUser);
        String hostName = getUserDisplayName(currentUser);

        Log.d(TAG, "=== 업로드 시작 ===");
        Log.d(TAG, "LoginHelper.isLoggedIn: " + LoginHelper.isLoggedIn(this));
        Log.d(TAG, "Firebase User: " + currentUser);
        Log.d(TAG, "Host UID: " + hostUid);
        Log.d(TAG, "Host Name: " + hostName);

        // UI 업데이트
        btnSubmit.setEnabled(false);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (selectedImageUris.isEmpty()) {
            // 이미지가 없으면 바로 Firestore에 저장
            savePostToFirestore(title, time, count, location, content, new ArrayList<>(), hostUid, hostName);
        } else {
            // 이미지 업로드 후 Firestore에 저장
            uploadImages(title, time, count, location, content, hostUid, hostName);
        }
    }

    private void uploadImages(String title, String time, int count, String location, String content, String hostUid, String hostName) {
        uploadedImageUrls.clear();
        int[] uploadCount = {0};
        int totalImages = selectedImageUris.size();

        Toast.makeText(this, "이미지 업로드 중... (0/" + totalImages + ")", Toast.LENGTH_SHORT).show();

        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri imageUri = selectedImageUris.get(i);
            uploadSingleImage(imageUri, i, uploadCount, totalImages, title, time, count, location, content, hostUid, hostName);
        }
    }

    private void uploadSingleImage(Uri imageUri, int imageIndex, int[] uploadCount, int totalImages,
                                   String title, String time, int count, String location, String content, String hostUid, String hostName) {

        String fileName = "post_images/" + System.currentTimeMillis() + "_" + imageIndex + ".jpg";
        StorageReference imageRef = storage.getReference().child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        uploadedImageUrls.add(downloadUri.toString());
                        uploadCount[0]++;

                        Toast.makeText(this, "이미지 업로드 중... (" + uploadCount[0] + "/" + totalImages + ")", Toast.LENGTH_SHORT).show();

                        if (uploadCount[0] == totalImages) {
                            // 모든 이미지 업로드 완료
                            savePostToFirestore(title, time, count, location, content, uploadedImageUrls, hostUid, hostName);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "다운로드 URL 가져오기 실패", e);
                        handleImageUploadComplete(uploadCount, totalImages, title, time, count, location, content, hostUid, hostName);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "이미지 업로드 실패", e);
                    handleImageUploadComplete(uploadCount, totalImages, title, time, count, location, content, hostUid, hostName);
                });
    }

    private void handleImageUploadComplete(int[] uploadCount, int totalImages, String title, String time, int count,
                                           String location, String content, String hostUid, String hostName) {
        uploadCount[0]++;
        if (uploadCount[0] == totalImages) {
            Toast.makeText(this, "일부 이미지 업로드 실패. 게시물 저장을 계속 진행합니다.", Toast.LENGTH_SHORT).show();
            savePostToFirestore(title, time, count, location, content, uploadedImageUrls, hostUid, hostName);
        }
    }

    private void savePostToFirestore(String title, String time, int count, String location, String content,
                                     List<String> imageUrls, String hostUid, String hostName) {

        Log.d(TAG, "Firestore에 게시물 저장 중...");

        Post newPost = new Post(title, time, count, location, content, imageUrls, new Date(), hostUid, hostName);

        db.collection("posts")
                .add(newPost)
                .addOnSuccessListener(documentReference -> {
                    String postId = documentReference.getId();
                    Log.d(TAG, "게시물 저장 성공. ID: " + postId);

                    Toast.makeText(this, "게시물 작성 완료!", Toast.LENGTH_SHORT).show();

                    // DetailActivity로 이동
                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra("postId", postId);
                    startActivity(intent);

                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "게시물 저장 실패", e);
                    Toast.makeText(this, "게시물 저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    // UI 복원
                    btnSubmit.setEnabled(true);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private String getUserId(FirebaseUser user) {
        if (user != null && user.getUid() != null) {
            return user.getUid();
        }

        // SharedPreferences에서 저장된 anonymous UID 가져오기
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String savedUid = prefs.getString("anonymous_uid", null);

        if (savedUid == null) {
            // 처음 생성하는 경우만 새로 만들고 저장
            savedUid = "anonymous_" + System.currentTimeMillis();
            prefs.edit().putString("anonymous_uid", savedUid).apply();
            Log.d(TAG, "새로운 anonymous UID 생성: " + savedUid);
        } else {
            Log.d(TAG, "기존 anonymous UID 사용: " + savedUid);
        }

        return savedUid;
    }

    private String getUserDisplayName(FirebaseUser user) {
        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                return user.getDisplayName();
            } else if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                return user.getEmail().split("@")[0];
            }
        }
        return "사용자";
    }
}