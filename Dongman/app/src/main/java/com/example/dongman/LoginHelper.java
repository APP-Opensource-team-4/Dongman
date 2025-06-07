package com.example.dongman;

import android.content.Context;

/** “로그인 했는지” 를 오직 SharedPreferences 로만 관리 */
public final class LoginHelper {

    private static final String PREF = "login_pref";
    private static final String KEY  = "logged_in";

    /** true 저장 */
    public static void setLoggedIn(Context c, boolean b){
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY, b).apply();
    }

    /** 로그인 여부 */
    public static boolean isLoggedIn(Context c){
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY, false);
    }

    private LoginHelper(){}
}
