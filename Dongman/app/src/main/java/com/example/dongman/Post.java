package com.example.dongman;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post implements Serializable {

    public String id; // Firestore 문서 ID를 저장하기 위한 필드
    public String title;
    public String time;       // 예: "오전 9시"
    public int count;         // 예: 10 (모집 인원)
    public String location;   // 예: "장소 입력"
    public String content;    // 모임 소개 (et_intro)
    public List<String> imageUrls; // Firebase Storage에 업로드된 이미지 URL 리스트
    public Date timestamp; // 게시물 생성 시간을 저장하여 정렬에 사용합니다.

    // Firestore는 객체 매핑을 위해 public 기본 생성자를 필요로 합니다.
    public Post() {
        this.imageUrls = new ArrayList<>(); // 리스트 초기화
    }

    // 모든 필드를 포함하는 생성자 (선택 사항: 필요한 경우 사용)
    public Post(String title, String time, int count, String location, String content, List<String> imageUrls, Date timestamp) {
        this.title = title;
        this.time = time;
        this.count = count;
        this.location = location;
        this.content = content;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.timestamp = timestamp;
    }

    // 모든 필드에 대한 Getter와 Setter (Firestore POJO 매핑을 위해 권장)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}