package com.example.dongman;

public class BoardPost {
    public final String postId;
    public final String title;
    public final String preview;
    public final String time;
    public final int viewCount;                // 🔸 필드명 변경!

    public BoardPost(String postId, String title,
                     String preview, String time,
                     int viewCount) {          // 🔸 매개변수도 교체
        this.postId = postId;
        this.title = title;
        this.preview = preview;
        this.time = time;
        this.viewCount = viewCount;
    }

    public int getViewCount() { return viewCount; }
}