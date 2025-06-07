// SignupActivity.java
package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText editId, editPw, editPwConfirm, editName, editPhone, editCode;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 툴바 뒤로가기
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        // EditText 연결
        editId = findViewById(R.id.editId);
        editPw = findViewById(R.id.editPw);
        editPwConfirm = findViewById(R.id.editPwConfirm);
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editCode = findViewById(R.id.editCode);

        Button btnSignUp = findViewById(R.id.btnSignUp);
        db = FirebaseFirestore.getInstance();

        btnSignUp.setOnClickListener(v -> {
            String id = editId.getText().toString().trim();
            String pw = editPw.getText().toString().trim();
            String pwConfirm = editPwConfirm.getText().toString().trim();
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String code = editCode.getText().toString().trim();

            if (id.isEmpty() || pw.isEmpty() || pwConfirm.isEmpty() || name.isEmpty() || phone.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pw.equals(pwConfirm)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> user = new HashMap<>();
            user.put("id", id);
            user.put("password", pw);
            user.put("name", name);
            user.put("phone", phone);
            user.put("code", code);

            db.collection("users")
                    .add(user)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "회원가입 완료: " + documentReference.getId());
                        Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, InterestActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "회원가입 실패", e);
                        Toast.makeText(this, "회원가입 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
