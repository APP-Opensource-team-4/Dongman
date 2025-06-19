package com.example.dongman;

public class RecentMeeting {
    public String title;       // 모임 제목
    public String description; // 모임 설명 or 본문
    public String tags;        // 위치 + 멤버 수 등 태그 역할
    public String imageUrl;    // 첫 번째 이미지 URL

    public RecentMeeting(String title, String description, String tags, String imageUrl) {
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.imageUrl = imageUrl;
    }

    // Getter (필요시)
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTags() { return tags; }
    public String getImageUrl() { return imageUrl; }
}
