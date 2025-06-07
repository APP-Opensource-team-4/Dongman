// LoginPrefs.java
package com.example.dongman;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;      // ← 추가

public final class LoginPrefs {
    private static final String SP  = "login_pref";
    private static final String KEY = "logged_in";

    /** Firebase 세션 or SharedPreferences 둘 중 하나라도 true 면 로그인 */
    public static boolean isLoggedIn(Context c) {
        boolean firebase = FirebaseAuth.getInstance().getCurrentUser() != null;
        if (firebase) return true;

        SharedPreferences sp = c.getSharedPreferences(SP, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY, false);
    }

    /** 이메일-비번 로그인처럼 자체 세션을 쓸 때만 호출 */
    public static void setLoggedIn(Context c, boolean v) {
        c.getSharedPreferences(SP, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY, v).apply();
    }
    private LoginPrefs(){}
}
