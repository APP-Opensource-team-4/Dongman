package com.example.dongman;

import java.util.Date;

public class Message {
    public enum Type {
        LEFT, RIGHT
    }

    private String senderId;
    private String senderName;
    private String content;
    private Date timestamp;
    private Type type;

    public Message() {}

    public Message(String senderId, String senderName, String content) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
}