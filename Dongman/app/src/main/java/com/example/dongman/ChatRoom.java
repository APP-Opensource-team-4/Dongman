package com.example.dongman;

import java.util.List;

public class ChatRoom {
    private String id;
    private String lastMessage;
    private com.google.firebase.Timestamp lastTime;
    private List<String> participants;   // 두 명의 UID
    private String postTitle;

    public ChatRoom() {}                 // Firestore 직렬화용 빈 생성자

    /* getter / setter */
    public String getId()             { return id; }  public void setId(String id){this.id=id;}
    public String getLastMessage()    { return lastMessage; }
    public com.google.firebase.Timestamp getLastTime() { return lastTime; }
    public List<String> getParticipants() { return participants; }
    public String getPostTitle()      { return postTitle; }
}

