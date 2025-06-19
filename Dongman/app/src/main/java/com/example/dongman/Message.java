package com.example.dongman;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Message {
    public enum Type { LEFT, RIGHT } // LEFT: 상대방 메시지, RIGHT: 내가 보낸 메시지

    private String senderId; // Firebase 사용자 ID
    private String sender;   // 발신자 이름 (채팅방에 표시될 이름)
    private String text;     // 메시지 내용
    @ServerTimestamp // Firestore 서버 시간으로 타임스탬프 자동 생성
    private Date timestamp;

    // UI 표시용 (Firestore에 저장되지 않음)
    private Type type;

    public Message() {
        // Firestore가 객체를 역직렬화할 때 필요한 public no-argument constructor
    }

    // Firebase Firestore용 생성자 (senderId, sender, text 포함)
    public Message(String senderId, String sender, String text) {
        this.senderId = senderId;
        this.sender = sender;
        this.text = text;
        // type과 timestamp는 Firestore에서 로드하거나, 서버에서 자동으로 생성
    }

    // Getters for Firestore (필수)
    public String getSenderId() { return senderId; }
    public String getSender() { return sender; }
    public String getText() { return text; }
    public Date getTimestamp() { return timestamp; }

    // Setters for Firestore (선택 사항이지만, 명시적으로 정의하는 것이 좋음)
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSender(String sender) { this.sender = sender; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    // UI 표시용 Getter/Setter (Firestore와 무관)
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
}