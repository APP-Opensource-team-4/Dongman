package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FieldValue; // Import FieldValue

import java.util.Date;

public class PostWriteActivity extends AppCompatActivity {

    private EditText etTitle, etCount, etLocation, etIntro;
    private Spinner spinnerTime;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.et_title);
        spinnerTime = findViewById(R.id.spinner_time);
        etCount = findViewById(R.id.et_count);
        etLocation = findViewById(R.id.et_location);
        etIntro = findViewById(R.id.et_intro);
        btnSubmit = findViewById(R.id.btn_submit);

        toolbar.setNavigationOnClickListener(v -> finish());

        String[] timeOptions = {"오전 9시", "오전 10시", "오전 11시", "오후 12시", "오후 1시",
                "오후 2시", "오후 3시", "오후 4시", "오후 5시", "오후 6시"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, timeOptions);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(timeAdapter);

        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {
        String title = etTitle.getText().toString().trim();
        String time = spinnerTime.getSelectedItem().toString();
        String countStr = etCount.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String intro = etIntro.getText().toString().trim();

        if (title.isEmpty() || countStr.isEmpty() || location.isEmpty() || intro.isEmpty()) {
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

        Post newPost = new Post();
        newPost.title = title;
        newPost.meta = intro;
        newPost.location = location + " • " + time + " • 멤버 " + count + "명";
        newPost.imageRes = R.drawable.placeholder_thumbnail; // Set a default image or allow user to pick one
        newPost.timestamp = new Date(); // ✨ Use server-side timestamp

        Intent resultIntent = new Intent();
        resultIntent.putExtra("post", newPost);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}