package com.example.dongman;

public class BoardPost {
    public final String postId;
    public final String title;
    public final String preview;
    public final String time;
    public final int commentCount;

    public BoardPost(String postId, String title, String preview, String time, int commentCount) {
        this.postId = postId;
        this.title = title;
        this.preview = preview;
        this.time = time;
        this.commentCount = commentCount;
    }

    // getter 필요 시 추가해도 돼
    public String getPostId() { return postId; }
    public String getTitle() { return title; }
    public String getPreview() { return preview; }
    public String getTime() { return time; }
    public int getCommentCount() { return commentCount; }
}
