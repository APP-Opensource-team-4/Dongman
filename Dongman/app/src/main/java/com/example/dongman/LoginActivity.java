package com.example.dongman;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Handler import 추가
import android.os.Looper; // Looper import 추가
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPw;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);

        ((Toolbar)findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        edtEmail = findViewById(R.id.edt_email);
        edtPw    = findViewById(R.id.edt_password);

        // ✨ Activity 시작 시 edtEmail에 포커스를 주고 키보드 띄우기
        if (edtEmail != null) {
            // ✨ (선택 사항) 혹시 모를 다른 뷰의 포커스를 초기화
            edtEmail.clearFocus();
            edtPw.clearFocus();

            // ✨ Handler를 사용하여 포커스 요청과 키보드 표시를 약간 딜레이 시킵니다.
            // UI가 완전히 준비된 후 키보드를 띄우는 데 도움이 될 수 있습니다.
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                edtEmail.requestFocus(); // 이메일 입력창에 포커스를 요청

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(edtEmail, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100); // 100밀리초 딜레이 (조정 가능)
        }

        findViewById(R.id.btn_login).setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pw    = edtPw.getText().toString().trim();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("email", email)
                    .whereEqualTo("password", pw)
                    .get()
                    .addOnSuccessListener(q -> {
                        if (q.isEmpty()) {
                            toast("로그인 실패: 정보가 일치하지 않습니다.");
                        } else {
                            // LoginHelper.setLoggedIn(this,true); // 이 줄을 삭제했습니다.
                            toast("로그인 성공!");
                            startActivity(new Intent(this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> toast("로그인 오류: "+e.getMessage()));
        });

        findViewById(R.id.btn_signup)
                .setOnClickListener(v -> startActivity(
                        new Intent(this, SignupActivity.class)));
    }
    private void toast(String msg){ Toast.makeText(this,msg,Toast.LENGTH_SHORT).show(); }
}