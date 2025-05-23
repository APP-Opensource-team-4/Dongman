package com.example.dongman;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_chat);           // xml 파일

        Toolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        // 공지 드롭다운 항목
        AutoCompleteTextView spin = findViewById(R.id.spinner_notice);
        spin.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new String[]{"5월 넷째주 모임 일정"}));

        // 더미 메시지
        List<Message> dummy = new ArrayList<>();
        dummy.add(new Message(Message.Type.LEFT, "동만이", "이번주 모임 공지 올라왔습니다!"));
        dummy.add(new Message(Message.Type.RIGHT, null, "네 알겠습니다~"));

        RecyclerView rv = findViewById(R.id.recycler_chat);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new ChatAdapter(dummy));
    }
}
