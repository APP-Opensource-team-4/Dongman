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
import com.google.firebase.firestore.DocumentReference;
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

    // DetailActivity에서 전달받는 정보
    private String chatRoomId;
    private String otherUserId;
    private String otherUserName;
    private String postTitle;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // DetailActivity에서 전달받은 데이터 가져오기
        getIntentData();

        // 툴바 설정
        setupToolbar();

        // 스피너 설정 (모임 제목 표시)
        setupSpinner();

        // 채팅방 컬렉션 설정
        setupChatCollection();

        // UI 요소 연결
        initializeUI();

        // 사용자 확인 및 채팅 로드
        checkUserAndLoadChat();
    }

    private void getIntentData() {
        chatRoomId = getIntent().getStringExtra("chatRoomId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");
        postTitle = getIntent().getStringExtra("postTitle");

        Log.d(TAG, "ChatRoom ID: " + chatRoomId);
        Log.d(TAG, "Other User: " + otherUserName);
        Log.d(TAG, "Post Title: " + postTitle);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(otherUserName != null ? otherUserName + "와의 채팅" : "채팅");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupSpinner() {
        AutoCompleteTextView spinner = findViewById(R.id.spinner_notice);
        if (spinner != null && postTitle != null) {
            spinner.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    new String[]{postTitle}));
        }
    }

    private void setupChatCollection() {
        // 1:1 채팅방 컬렉션 설정
        if (chatRoomId != null && !chatRoomId.isEmpty()) {
            /* 1:1 · 그룹 공통 ─ chats/{roomId}/messages */
            chatCollection = db.collection("chats")
                    .document(chatRoomId)
                    .collection("messages");
        } else {
            /* (옵션) 전체 채팅용 */
            chatCollection = db.collection("chats_global");
        }
    }

    private void initializeUI() {
        chatRecyclerView = findViewById(R.id.recycler_chat);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);

        // RecyclerView 설정
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // 새로운 메시지가 아래에 표시되도록
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // 메시지 전송 버튼 리스너
        if (sendButton != null) {
            sendButton.setOnClickListener(v -> sendMessage());
        }
    }

    private void checkUserAndLoadChat() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Log.d(TAG, "Firebase User check: " + (currentUser != null ? currentUser.getUid() : "NULL"));

        if (currentUser != null) {
            // Firebase Auth에 로그인되어 있음
            currentUserName = currentUser.getDisplayName();
            if (currentUserName == null || currentUserName.isEmpty()) {
                currentUserName = currentUser.getEmail() != null ?
                        currentUser.getEmail().split("@")[0] : "사용자";
            }
            Log.d(TAG, "User logged in as: " + currentUserName);
            loadChatMessages();
        } else {
            // Firebase Auth에 로그인되어 있지 않음 - 익명 로그인 시도
            Log.d(TAG, "No Firebase user, attempting anonymous login");
            signInAnonymously();
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Anonymous sign in successful");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            currentUserName = "익명사용자_" + user.getUid().substring(0, 6);
                            loadChatMessages();
                        }
                    } else {
                        Log.e(TAG, "Anonymous sign in failed", task.getException());
                        // 익명 로그인도 실패하면 기본 사용자로 진행
                        currentUserName = "게스트";
                        loadChatMessages();
                    }
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 현재 사용자 정보 확인
        String senderId = "guest";
        String senderName = currentUserName != null ? currentUserName : "게스트";

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            senderId = user.getUid();
        }

        Message msg = new Message(senderId, senderName, messageText);

        chatCollection.add(msg)
                .addOnSuccessListener(d -> {
                    // 🔻 chats/{roomId} 문서 업데이트
                    db.collection("chats").document(chatRoomId)
                            .update("lastMessage", messageText,
                                    "lastTime", com.google.firebase.Timestamp.now());

                    messageEditText.setText("");
                    chatRecyclerView.scrollToPosition(messageList.size()-1);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,"메시지 전송 실패",Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"send fail",e);
                });
    }

    private void loadChatMessages() {
        Log.d(TAG, "Loading chat messages...");

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

                        if (snapshots != null) {
                            messageList.clear();
                            for (QueryDocumentSnapshot doc : snapshots) {
                                Message message = doc.toObject(Message.class);

                                // 현재 사용자의 메시지인지 확인하여 타입 설정
                                String currentUserId = "guest";
                                if (mAuth.getCurrentUser() != null) {
                                    currentUserId = mAuth.getCurrentUser().getUid();
                                }

                                if (Objects.equals(message.getSenderId(), currentUserId)) {
                                    message.setType(Message.Type.RIGHT); // 내 메시지는 오른쪽
                                } else {
                                    message.setType(Message.Type.LEFT);  // 상대방 메시지는 왼쪽
                                }
                                messageList.add(message);
                            }

                            chatAdapter.notifyDataSetChanged();

                            // 새로운 메시지가 있으면 스크롤
                            if (messageList.size() > 0) {
                                chatRecyclerView.scrollToPosition(messageList.size() - 1);
                            }

                            Log.d(TAG, "Loaded " + messageList.size() + " messages");
                        }
                    }
                });
    }
}