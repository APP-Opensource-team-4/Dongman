package com.example.dongman;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout; // Keep this import
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostWriteActivity extends AppCompatActivity {

    // UI 요소
    private EditText etTitle, etCount, etLocation, etIntro;
    private Spinner spinnerTime;
    private Button btnSubmit;
    private ImageButton btnAddPhoto;
    private TextView tvPhotoCount;
    private LinearLayout layoutImagePreviews;

    // Firebase 인스턴스
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // 이미지 업로드를 위한 변수
    private List<Uri> selectedImageUris = new ArrayList<>();
    private List<String> uploadedImageUrls = new ArrayList<>();
    private static final int MAX_PHOTOS = 5;

    // 갤러리에서 이미지를 선택하기 위한 ActivityResultLauncher
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) { // 다중 이미지 선택
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            if (selectedImageUris.size() < MAX_PHOTOS) {
                                selectedImageUris.add(imageUri);
                            } else {
                                Toast.makeText(this, "최대 " + MAX_PHOTOS + "장까지만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    } else if (result.getData().getData() != null) { // 단일 이미지 선택
                        Uri imageUri = result.getData().getData();
                        if (selectedImageUris.size() < MAX_PHOTOS) {
                            selectedImageUris.add(imageUri);
                        } else {
                            Toast.makeText(this, "최대 " + MAX_PHOTOS + "장까지만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    updatePhotoPreviewAndCount();
                }
            });

    // 런타임 권한 요청을 위한 ActivityResultLauncher
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGalleryForImage();
                } else {
                    Toast.makeText(this, "사진 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Firebase 초기화
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // UI 요소 연결
        Toolbar toolbar = findViewById(R.id.toolbar);
        btnAddPhoto = findViewById(R.id.btn_add_photo);
        tvPhotoCount = findViewById(R.id.tv_photo_count);
        layoutImagePreviews = findViewById(R.id.layout_image_previews);
        etTitle = findViewById(R.id.et_title);
        spinnerTime = findViewById(R.id.spinner_time);
        etCount = findViewById(R.id.et_count);
        etLocation = findViewById(R.id.et_location);
        etIntro = findViewById(R.id.et_intro);
        btnSubmit = findViewById(R.id.btn_submit);

        // 툴바 뒤로가기 버튼 설정
        toolbar.setNavigationOnClickListener(v -> finish());

        // 시간 스피너 설정
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_options, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(timeAdapter);

        // 사진 추가 버튼 클릭 리스너
        btnAddPhoto.setOnClickListener(v -> checkAndRequestPermissions());

        // 작성 완료 버튼 클릭 리스너
        btnSubmit.setOnClickListener(v -> handleSubmit());

        // 초기 사진 개수 업데이트
        updatePhotoPreviewAndCount();
    }

    // 권한 확인 및 요청
    private void checkAndRequestPermissions() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGalleryForImage();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }


    // 갤러리 열기 (다중 이미지 선택 허용)
    private void openGalleryForImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 다중 이미지 선택 허용
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // URI 권한 부여
        pickImageLauncher.launch(Intent.createChooser(intent, "이미지 선택"));
    }

    // 사진 미리보기 및 개수 업데이트
    private void updatePhotoPreviewAndCount() {
        tvPhotoCount.setText(selectedImageUris.size() + "/" + MAX_PHOTOS);

        layoutImagePreviews.removeAllViews(); // 기존 미리보기 뷰 모두 제거

        for (Uri uri : selectedImageUris) {
            ConstraintLayout imageContainer = new ConstraintLayout(this);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.image_preview_size),
                    (int) getResources().getDimension(R.dimen.image_preview_size)
            );
            containerParams.setMargins(0, 0, (int) getResources().getDimension(R.dimen.image_preview_margin), 0);
            imageContainer.setLayoutParams(containerParams);

            ImageView previewImageView = new ImageView(this);
            previewImageView.setLayoutParams(new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
            ));
            previewImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(uri).into(previewImageView);
            imageContainer.addView(previewImageView);

            // DELETE BUTTON LOGIC - CORRECTION STARTS HERE
            ImageButton deleteButton = new ImageButton(this);
            deleteButton.setBackgroundResource(android.R.color.transparent);
            deleteButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);

            // Correct way to set ConstraintLayout.LayoutParams properties programmatically
            ConstraintLayout.LayoutParams deleteParams = new ConstraintLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.delete_button_size),
                    (int) getResources().getDimension(R.dimen.delete_button_size)
            );

            // Use the public fields for constraints directly
            deleteParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            deleteParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID; // Corrected: topToTop, not topToTopOf

            deleteButton.setLayoutParams(deleteParams);
            deleteButton.setOnClickListener(v -> {
                selectedImageUris.remove(uri);
                updatePhotoPreviewAndCount();
            });
            imageContainer.addView(deleteButton);
            // DELETE BUTTON LOGIC - CORRECTION ENDS HERE

            layoutImagePreviews.addView(imageContainer);
        }
    }


    private void handleSubmit() {
        // 입력 필드 유효성 검사
        String title = etTitle.getText().toString().trim();
        String time = spinnerTime.getSelectedItem().toString();
        String countStr = etCount.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String content = etIntro.getText().toString().trim();

        if (title.isEmpty() || countStr.isEmpty() || location.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int count;
        try {
            count = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "모집 인원은 숫자로 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 이미지 업로드 시작 (이미지가 없으면 바로 Firestore에 저장)
        uploadImagesToFirebaseStorage(title, time, count, location, content);
    }

    private void uploadImagesToFirebaseStorage(String title, String time, int count, String location, String content) {
        if (selectedImageUris.isEmpty()) {
            // 이미지가 없으면 바로 Firestore에 저장
            savePostToFirestore(title, time, count, location, content, new ArrayList<>());
            return;
        }

        uploadedImageUrls.clear(); // 기존 업로드 URL 리스트 초기화
        final int[] uploadCount = {0}; // 업로드된 이미지 개수 카운터

        Toast.makeText(this, "이미지 업로드 중...", Toast.LENGTH_LONG).show();
        btnSubmit.setEnabled(false); // 업로드 중 버튼 비활성화

        for (Uri uri : selectedImageUris) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            // 파일명에 고유성을 더하기 위해 UUID를 사용하거나, 더 확실한 방법으로 변경할 수 있습니다.
            // 여기서는 System.currentTimeMillis()와 timestamp를 결합합니다.
            String fileName = "post_images/" + System.currentTimeMillis() + "_" + timestamp + ".jpg";
            StorageReference imageRef = storageRef.child(fileName);

            UploadTask uploadTask = imageRef.putFile(uri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    uploadedImageUrls.add(downloadUri.toString());
                    uploadCount[0]++;

                    if (uploadCount[0] == selectedImageUris.size()) {
                        // 모든 이미지 업로드가 완료되면 게시물 저장
                        savePostToFirestore(title, time, count, location, content, uploadedImageUrls);
                        btnSubmit.setEnabled(true); // 업로드 완료 후 버튼 다시 활성화
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "다운로드 URL 가져오기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // 오류 발생해도 카운트 증가, 모든 이미지 업로드 시도 후 게시물 저장 시도
                    uploadCount[0]++;
                    if (uploadCount[0] == selectedImageUris.size()) {
                        Toast.makeText(this, "일부 이미지 업로드 실패. 게시물 저장 시도.", Toast.LENGTH_SHORT).show();
                        savePostToFirestore(title, time, count, location, content, uploadedImageUrls);
                        btnSubmit.setEnabled(true);
                    }
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                // 오류 발생해도 카운트 증가, 모든 이미지 업로드 시도 후 게시물 저장 시도
                uploadCount[0]++;
                if (uploadCount[0] == selectedImageUris.size()) {
                    Toast.makeText(this, "일부 이미지 업로드 실패. 게시물 저장 시도.", Toast.LENGTH_SHORT).show();
                    savePostToFirestore(title, time, count, location, content, uploadedImageUrls);
                    btnSubmit.setEnabled(true);
                }
            });
        }
    }

    private void savePostToFirestore(String title, String time, int count, String location, String content, List<String> imageUrls) {
        Post newPost = new Post(title, time, count, location, content, imageUrls, new Date());

        db.collection("posts")
                .add(newPost)
                .addOnSuccessListener(documentReference -> {
                    // Firestore 문서 ID를 Post 객체에 저장 (선택 사항)
                    newPost.setId(documentReference.getId()); // Post.java에 setId() 추가 필요
                    Toast.makeText(this, "게시물 작성 완료!", Toast.LENGTH_SHORT).show();
                    finish(); // Activity 종료
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "게시물 작성 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}