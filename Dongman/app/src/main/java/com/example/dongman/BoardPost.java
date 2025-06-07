package com.example.dongman;

public class BoardPost {
    public final String title, preview, time;
    public final int commentCount;

    public BoardPost(String title, String preview, String time, int commentCount) {
        this.title = title;
        this.preview = preview;
        this.time = time;
        this.commentCount = commentCount;
    }
}
