package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPw;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);

        ((Toolbar)findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        edtEmail = findViewById(R.id.edt_email);
        edtPw    = findViewById(R.id.edt_password);

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
                            LoginHelper.setLoggedIn(this,true);
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
