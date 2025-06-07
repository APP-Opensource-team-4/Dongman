package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText idEt,pwEt,pw2Et,nameEt,phoneEt,codeEt;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_signup);
        ((Toolbar)findViewById(R.id.toolbar)).setNavigationOnClickListener(v->finish());

        idEt   = findViewById(R.id.editId);
        pwEt   = findViewById(R.id.editPw);
        pw2Et  = findViewById(R.id.editPwConfirm);
        nameEt = findViewById(R.id.editName);
        phoneEt= findViewById(R.id.editPhone);
        codeEt = findViewById(R.id.editCode);

        findViewById(R.id.btnSignUp).setOnClickListener(v -> {
            String id=idEt.getText().toString().trim();
            String pw=pwEt.getText().toString().trim();
            String pw2=pw2Et.getText().toString().trim();
            String name=nameEt.getText().toString().trim();
            String phone=phoneEt.getText().toString().trim();
            String code=codeEt.getText().toString().trim();

            if(id.isEmpty()||pw.isEmpty()||pw2.isEmpty()||name.isEmpty()
                    ||phone.isEmpty()||code.isEmpty()){
                toast("모든 항목을 입력해주세요."); return;
            }
            if(!pw.equals(pw2)){ toast("비밀번호가 일치하지 않습니다."); return; }

            Map<String,Object> u=new HashMap<>();
            u.put("email",id); u.put("password",pw);
            u.put("name",name); u.put("phone",phone); u.put("code",code);

            FirebaseFirestore.getInstance().collection("users")
                    .add(u)
                    .addOnSuccessListener(r -> {
                        toast("회원가입 성공!");
                        startActivity(new Intent(this, InterestActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> toast("회원가입 실패: "+e.getMessage()));
        });
    }
    private void toast(String m){ Toast.makeText(this,m,Toast.LENGTH_SHORT).show(); }
}
