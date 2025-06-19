package com.example.dongman;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference; // 추가
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference chatCollection;

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    private EditText messageEditText;
    private ImageButton sendButton;

    // 테스트용 사용자 정보 (실제 앱에서는 로그인 화면에서 가져오거나 Firebase Auth 사용)
    private String TEST_USER_EMAIL = "testuser@example.com";
    private String TEST_USER_PASSWORD = "password123";
    private String CURRENT_USER_NAME = "테스트 사용자";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        AutoCompleteTextView spin = findViewById(R.id.spinner_notice);
        if (spin != null) {
            spin.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    new String[]{"5월 넷째주 모임 일정"}));
        }

        // --- Firebase 초기화 ---
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        chatCollection = db.collection("chats"); // 'chats' 컬렉션 사용

        // --- UI 요소 연결 ---
        chatRecyclerView = findViewById(R.id.recycler_chat);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);

        // --- RecyclerView 설정 ---
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // 새로운 메시지가 아래에 표시되도록
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // --- 메시지 전송 버튼 리스너 ---
        if (sendButton != null) {
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage();
                }
            });
        }

        // --- 사용자 인증 (테스트용) ---
        if (mAuth.getCurrentUser() != null) {
            CURRENT_USER_NAME = Objects.requireNonNull(mAuth.getCurrentUser().getEmail()).split("@")[0];
            loadChatMessages();
        } else {
            signInAnonymously(); // 익명 로그인 시도
            // 또는 signInUser(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInAnonymously:success");
                            loadChatMessages();
                        } else {
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(ChatActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.getEmail() != null) {
                                CURRENT_USER_NAME = user.getEmail().split("@")[0];
                            } else {
                                CURRENT_USER_NAME = "알 수 없는 사용자";
                            }
                            loadChatMessages();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(ChatActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인되어 있지 않습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Message message = new Message(user.getUid(), CURRENT_USER_NAME, messageText);

        chatCollection.add(message)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() { // 변경된 부분
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) { // 변경된 부분
                        if (task.isSuccessful()) {
                            messageEditText.setText("");
                            chatRecyclerView.scrollToPosition(messageList.size() - 1);
                        } else {
                            Log.e(TAG, "메시지 전송 실패", task.getException());
                            Toast.makeText(ChatActivity.this, "메시지 전송 실패!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadChatMessages() {
        chatCollection
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "메시지 로드 실패", e);
                            Toast.makeText(ChatActivity.this, "메시지 로드 실패!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        messageList.clear();
                        for (QueryDocumentSnapshot doc : Objects.requireNonNull(snapshots)) {
                            Message message = doc.toObject(Message.class);
                            if (mAuth.getCurrentUser() != null && Objects.equals(message.getSenderId(), mAuth.getCurrentUser().getUid())) {
                                message.setType(Message.Type.RIGHT);
                            } else {
                                message.setType(Message.Type.LEFT);
                            }
                            messageList.add(message);
                        }
                        chatAdapter.notifyDataSetChanged();
                        if (messageList.size() > 0) {
                            chatRecyclerView.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }
}