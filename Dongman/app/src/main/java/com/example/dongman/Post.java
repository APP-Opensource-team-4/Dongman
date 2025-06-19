package com.example.dongman;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post implements Serializable {
    private String id; // Firestore document ID (set manually)
    private String title;
    private String content;
    private String location;
    private String time;
    private int count;
    private List<String> imageUrls;
    @ServerTimestamp
    private Date timestamp;
    private String hostUid;    // Firebase User ID (UID) of the meeting host
    private String hostName;   // Name of the meeting host (for display)

    // PostEditActivity에서 사용하는 필드들 추가
    private Date dateTime;     // 모임 날짜/시간
    private int maxParticipants; // 최대 참가자 수
    private Date updatedAt;    // 수정 시간

    private Integer memberCount;

    public Post() {
        this.imageUrls = new ArrayList<>();
    }

    public Post(String title, String time, int count, String location, String content, List<String> imageUrls, Date timestamp, String hostUid, String hostName) {
        this.title = title;
        this.time = time;
        this.count = count;
        this.location = location;
        this.content = content;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.timestamp = timestamp;
        this.hostUid = hostUid;
        this.hostName = hostName;
        this.maxParticipants = count; // count를 maxParticipants로 매핑
    }

    // 기존 Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCount() {                  // 멤버 수 없으면 0
        return memberCount == null ? 0 : memberCount;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getHostUid() {
        return hostUid;
    }

    public void setHostUid(String hostUid) {
        this.hostUid = hostUid;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    // 새로 추가된 Getters and Setters
    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public int getMaxParticipants() {
        return maxParticipants != 0 ? maxParticipants : count; // maxParticipants가 없으면 count 사용
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
        this.count = maxParticipants; // 하위 호환성을 위해 count도 동기화
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper method to get the first image URL
    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : "";
    }
}