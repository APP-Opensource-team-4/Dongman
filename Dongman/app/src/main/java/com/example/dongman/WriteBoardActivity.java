// WriteBoardActivity.java
package com.example.dongman;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * 익명 자유게시판 글쓰기 화면
 *  - 제목·본문 입력
 *  - 우측 하단 “등록”(밑줄 텍스트) 클릭 → Firestore 업로드
 */
public class WriteBoardActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etContent;
    private TextView          btnPost;
    private ProgressBar       pbUpload;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_board);

        /* ────────── UI 바인딩 ────────── */
        Toolbar toolbar   = findViewById(R.id.toolbar);
        etTitle           = findViewById(R.id.et_title);
        etContent         = findViewById(R.id.et_content);
        btnPost           = findViewById(R.id.btn_post);      // 밑줄 텍스트
        pbUpload          = findViewById(R.id.pb_upload);

        /* ────────── 툴바 설정 ────────── */
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // xml title 사용
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        /* ────────── Firestore ────────── */
        db = FirebaseFirestore.getInstance();

        /* ────────── 등록 클릭 ────────── */
        btnPost.setOnClickListener(v -> uploadPost());
    }

    /** 게시글 업로드 */
    private void uploadPost() {
        String title   = etTitle.getText()   != null ? etTitle.getText().toString().trim()   : "";
        String content = etContent.getText() != null ? etContent.getText().toString().trim() : "";

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        setUiEnabled(false);

        Map<String, Object> data = new HashMap<>();
        data.put("title",   title);
        data.put("content", content);
        data.put("preview", content.length() > 40 ? content.substring(0, 40) + "…" : content);
        data.put("writer",  "익명");
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("commentCount", 0);   // 초기값
        data.put("likeCount",    0);
        data.put("viewCount",    0);

        FirebaseFirestore.getInstance()
                .collection("boards")          // ← 스크린샷과 동일한 컬렉션
                .add(data)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "게시글이 등록되었습니다", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);       // 성공 플래그
                    finish();                   // 목록으로 복귀
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "업로드 실패: " + e.getLocalizedMessage(),
                            Toast.LENGTH_SHORT).show();
                    setUiEnabled(true);
                });
    }


    /** 업로드 중 UI 잠금/복구 */
    private void setUiEnabled(boolean enabled) {
        etTitle.setEnabled(enabled);
        etContent.setEnabled(enabled);
        btnPost.setEnabled(enabled);
        btnPost.setAlpha(enabled ? 1f : 0.3f);
        pbUpload.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    /** Firestore 저장용 데이터 모델 (익명 게시판 규격) */
    private static class PostData {
        final String title;
        final String content;
        final String writer = "익명";
        final FieldValue timestamp = FieldValue.serverTimestamp();
        final String preview;          // 목록용 미리보기 (40자)

        PostData(String title, String content) {
            this.title   = title;
            this.content = content;
            this.preview = content.length() > 40
                    ? content.substring(0, 40) + "…"
                    : content;
        }
    }
}
