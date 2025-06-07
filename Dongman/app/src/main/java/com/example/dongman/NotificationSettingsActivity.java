package com.example.dongman;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

public class NotificationSettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_CHAT    = "chat";
    private static final String KEY_MEETING = "meeting";
    private static final String KEY_POST    = "post";
    private static final String KEY_EVENT   = "event";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        SwitchCompat swChat    = findViewById(R.id.sw_chat);
        SwitchCompat swMeeting = findViewById(R.id.sw_meeting);
        SwitchCompat swPost    = findViewById(R.id.sw_post);
        SwitchCompat swEvent   = findViewById(R.id.sw_event);

        swChat.setChecked(prefs.getBoolean(KEY_CHAT, true));
        swMeeting.setChecked(prefs.getBoolean(KEY_MEETING, true));
        swPost.setChecked(prefs.getBoolean(KEY_POST, true));
        swEvent.setChecked(prefs.getBoolean(KEY_EVENT, true));

        swChat.setOnCheckedChangeListener((b, isChecked) ->
                prefs.edit().putBoolean(KEY_CHAT, isChecked).apply());
        swMeeting.setOnCheckedChangeListener((b, isChecked) ->
                prefs.edit().putBoolean(KEY_MEETING, isChecked).apply());
        swPost.setOnCheckedChangeListener((b, isChecked) ->
                prefs.edit().putBoolean(KEY_POST, isChecked).apply());
        swEvent.setOnCheckedChangeListener((b, isChecked) ->
                prefs.edit().putBoolean(KEY_EVENT, isChecked).apply());
    }
}
