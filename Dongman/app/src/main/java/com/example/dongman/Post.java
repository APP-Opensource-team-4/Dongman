package com.example.dongman;

// Room 관련 import는 제거합니다.
// import androidx.room.ColumnInfo;
// import androidx.room.Entity;
// import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date; // Firestore의 Timestamp 필드를 위해 java.util.Date를 사용합니다.

public class Post implements Serializable {

    // Firestore는 문서 ID를 자동으로 관리하므로, id 필드는 선택 사항입니다.
    // 만약 문서 ID를 Post 객체 내에 저장하고 싶다면 String id; 를 추가할 수 있습니다.
    public String id; // Firestore 문서 ID를 저장하기 위한 필드 (선택 사항)

    public String title;
    public String meta;
    public String location;
    public int imageRes;

    public Date timestamp; // ✨ 게시물 생성 시간을 저장하여 정렬에 사용합니다.

    // Firestore는 객체 매핑을 위해 public 기본 생성자를 필요로 합니다.
    public Post() {
        // No-argument constructor required for Firestore
    }

    // 필요하다면 모든 필드를 포함하는 생성자를 추가할 수 있습니다.
    public Post(String title, String meta, String location, int imageRes, Date timestamp) {
        this.title = title;
        this.meta = meta;
        this.location = location;
        this.imageRes = imageRes;
        this.timestamp = timestamp;
    }

    // Firestore는 필드를 자동으로 매핑할 때 public 필드나 public getter/setter를 사용합니다.
    // 여기서는 필드가 public이므로 별도의 getter/setter는 필수는 아닙니다.
}