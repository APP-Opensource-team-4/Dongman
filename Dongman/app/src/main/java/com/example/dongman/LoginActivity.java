package com.example.dongman;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

        // 포커스 및 키보드 자동 띄우기
        if (edtEmail != null) {
            edtEmail.clearFocus();
            edtPw.clearFocus();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                edtEmail.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(edtEmail, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100);
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
                            // ✅ 로그인 상태 저장
                            LoginHelper.setLoggedIn(getApplicationContext(), true);
                            toast("로그인 성공!");

                            // ✅ MainActivity로 이동하며 스택 초기화
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> toast("로그인 오류: " + e.getMessage()));
        });

        findViewById(R.id.btn_signup)
                .setOnClickListener(v -> startActivity(
                        new Intent(this, SignupActivity.class)));
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
