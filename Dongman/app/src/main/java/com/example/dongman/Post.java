package com.example.dongman;

import java.io.Serializable;

public class Post implements Serializable {
    public String title;
    public String meta;
    public String location;
    public int imageRes;

    public Post() {
        // 기본 생성자
    }
}