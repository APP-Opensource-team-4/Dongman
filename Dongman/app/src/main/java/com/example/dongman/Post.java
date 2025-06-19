package com.example.dongman;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post implements Serializable {

    public String id;
    public String title;
    public String time;
    public int count;
    public String location;
    public String content;
    public List<String> imageUrls;
    public Date timestamp;

    public Post() {
        this.imageUrls = new ArrayList<>();
    }

    public Post(String title, String time, int count, String location, String content, List<String> imageUrls, Date timestamp) {
        this.title = title;
        this.time = time;
        this.count = count;
        this.location = location;
        this.content = content;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.timestamp = timestamp;
    }

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

    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }
}
