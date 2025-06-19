package com.example.dongman;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecentActivity extends AppCompatActivity {

    private static final String TAG = "RecentActivity";
    private static final String PREF_KEY = "recent_posts";

    private List<Post> recentList = new ArrayList<>();
    private RecentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent);

        /* 툴바: 뒤로가기 */
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        /* Recycler */
        RecyclerView rv = findViewById(R.id.rv_recent_meetings);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecentAdapter(this, recentList, pos -> {
            // 삭제 클릭 → SharedPreferences에서도 제거
            removeRecentAt(pos);
        });
        rv.setAdapter(adapter);

        loadRecentPosts();
    }

    /* SharedPreferences → List<Post> 로드 */
    private void loadRecentPosts() {
        SharedPreferences sp = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String json = sp.getString("list", null);
        if (json == null) return;

        Type listType = new TypeToken<ArrayList<Post>>(){}.getType();
        List<Post> stored = new Gson().fromJson(json, listType);
        if (stored != null) {
            Collections.reverse(stored);          // 최근 본 순서대로 (가장 마지막에 본 글이 위)
            recentList.clear();
            recentList.addAll(stored);
            adapter.notifyDataSetChanged();
        }
        Log.d(TAG, "recent size = " + recentList.size());
    }

    /* 삭제 후 SharedPreferences 갱신 */
    private void removeRecentAt(int pos) {
        recentList.remove(pos);
        adapter.notifyItemRemoved(pos);

        SharedPreferences sp = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        sp.edit().putString("list",
                new Gson().toJson(recentList)).apply();
    }
}
