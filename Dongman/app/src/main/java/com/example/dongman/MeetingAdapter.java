package com.example.dongman;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Glide import 추가

import java.util.List;

/**
 * RecyclerView 어댑터
 * Post 클래스의 변경된 필드 (time, count, content, imageUrls)에 맞춰 수정됨
 */
public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.VH> {

    private final List<Post> items;
    private final View.OnClickListener itemClick;

    public MeetingAdapter(List<Post> items, View.OnClickListener click) {
        this.items = items;
        this.itemClick = click;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_meeting.xml 레이아웃 파일을 사용
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meeting, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Post p = items.get(pos);

        // 1. 제목 설정
        h.title.setText(p.getTitle()); // Post.title getter 사용

        // 2. 모임 소개 (content 필드 사용)
        // item_content TextView가 "모임 소개"를 담는다고 가정
        if (p.getContent() != null && !p.getContent().isEmpty()) {
            h.content.setText(p.getContent()); // Post.content getter 사용
            h.content.setVisibility(View.VISIBLE);
        } else {
            h.content.setVisibility(View.GONE); // 내용이 없으면 숨김
        }

        // 3. 메타 정보 (장소, 시간, 모집 인원 조합)
        // item_meta TextView가 "장소 • 시간 • 멤버 N명" 같은 정보를 담는다고 가정
        // Post.location, Post.time, Post.count getter 사용
        String metaText = p.getLocation();
        if (p.getTime() != null && !p.getTime().isEmpty()) {
            metaText += " • " + p.getTime();
        }
        if (p.getCount() > 0) {
            metaText += " • 멤버 " + p.getCount() + "명";
        }
        h.meta.setText(metaText);


        // 4. 이미지 설정 (imageRes 대신 imageUrls 사용)
        // Glide를 사용하여 Firebase Storage URL에서 이미지 로드
        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
            String imageUrl = p.getImageUrls().get(0); // 첫 번째 이미지 URL 로드
            Glide.with(h.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_thumbnail) // 이제 placeholder_thumbnail 사용 가능
                    .error(R.drawable.placeholder_thumbnail)     // 에러 이미지도 placeholder_thumbnail 사용 가능
                    .into(h.thumb); // ViewHolder의 ImageView에 이미지 로드
        } else {
            // 이미지가 없을 경우 기본 이미지 설정
            h.thumb.setImageResource(R.drawable.placeholder_thumbnail); // 기본 이미지도 placeholder_thumbnail 사용 가능
        }

        // 아이템 클릭 리스너를 위해 Post 객체를 View의 태그로 설정
        h.itemView.setTag(p);
        h.itemView.setOnClickListener(itemClick);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder 클래스 정의
    static class VH extends RecyclerView.ViewHolder {
        ImageView thumb;
        TextView title, content, meta; // content는 모임 소개, meta는 장소/시간/인원 조합

        VH(View v) {
            super(v);
            // item_meeting.xml 레이아웃 파일의 ID와 정확히 일치해야 합니다.
            thumb   = v.findViewById(R.id.item_thumbnail); // ImageView
            title   = v.findViewById(R.id.item_title);     // TextView (제목)
            content = v.findViewById(R.id.item_content);   // TextView (모임 소개)
            meta    = v.findViewById(R.id.item_meta);      // TextView (장소, 시간, 인원)
        }
    }
}