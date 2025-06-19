package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private MeetingAdapter adapter;
    private List<Post> postList = new ArrayList<>();
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recycler_view);
        loadingBar = findViewById(R.id.loading_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MeetingAdapter(postList, this::onPostClick);
        recyclerView.setAdapter(adapter);

        loadPostsFromFirestore();
    }

    private void loadPostsFromFirestore() {
        loadingBar.setVisibility(View.VISIBLE);

        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setId(doc.getId()); // ✅ 문서 ID를 직접 지정
                            postList.add(post);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    loadingBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    loadingBar.setVisibility(View.GONE);
                });
    }

    private void onPostClick(View view) {
        Post clickedPost = (Post) view.getTag();
        if (clickedPost != null && clickedPost.getId() != null) {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("postId", clickedPost.getId()); // 🔥 전달
            startActivity(intent);
        }
    }
}
