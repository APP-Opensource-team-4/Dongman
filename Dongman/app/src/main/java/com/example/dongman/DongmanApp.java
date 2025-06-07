package com.example.dongman;

import android.app.Application;
import com.google.firebase.auth.FirebaseAuth;

public class DongmanApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        FirebaseAuth.getInstance().signOut();          // ① 앱 재실행 때마다 강제 로그아웃
        LoginHelper.setLoggedIn(this,false);
    }
}