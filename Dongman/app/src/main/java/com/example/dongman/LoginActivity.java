package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {
    private EditText editId, editPw;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        editId = findViewById(R.id.editId);
        editPw = findViewById(R.id.editPw);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView signUp = findViewById(R.id.textSignUp);

        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> {
            String id = editId.getText().toString().trim();
            String pw = editPw.getText().toString().trim();

            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("id", id)
                    .whereEqualTo("password", pw)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            for (QueryDocumentSnapshot doc : query) {
                                Log.d("Login", "로그인 성공: " + doc.getId());
                            }
                            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class)); // 메인화면으로 이동
                            finish();
                        } else {
                            Toast.makeText(this, "아이디 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Login", "로그인 오류", e);
                        Toast.makeText(this, "로그인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    });
        });

        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
