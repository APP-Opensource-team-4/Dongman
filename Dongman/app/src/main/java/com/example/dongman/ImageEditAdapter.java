package com.example.dongman;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageEditAdapter extends RecyclerView.Adapter<ImageEditAdapter.ImageViewHolder> {

    private List<String> imageUrls;
    private OnImageRemoveListener onImageRemoveListener;

    public interface OnImageRemoveListener {
        void onImageRemove(int position);
    }

    public ImageEditAdapter(List<String> imageUrls, OnImageRemoveListener listener) {
        this.imageUrls = imageUrls;
        this.onImageRemoveListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_edit, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // 이미지 로드 (URL 또는 URI 모두 처리)
        if (imageUrl.startsWith("http")) {
            // Firebase Storage URL
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_thumbnail)
                    .error(R.drawable.placeholder_thumbnail)
                    .into(holder.imageView);
        } else {
            // 로컬 URI
            Uri imageUri = Uri.parse(imageUrl);
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_thumbnail)
                    .error(R.drawable.placeholder_thumbnail)
                    .into(holder.imageView);
        }

        // 삭제 버튼 클릭 리스너
        holder.deleteButton.setOnClickListener(v -> {
            if (onImageRemoveListener != null) {
                onImageRemoveListener.onImageRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    public void updateImages(List<String> newImageUrls) {
        this.imageUrls = newImageUrls;
        notifyDataSetChanged();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_photo);
            deleteButton = itemView.findViewById(R.id.btn_delete_image);
        }
    }
}