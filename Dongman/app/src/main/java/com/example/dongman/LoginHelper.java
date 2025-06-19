package com.example.dongman;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginHelper {

    private static final String SHARED_PREF_NAME = "DongmanLoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    /**
     * Saves the user's login state to SharedPreferences.
     * @param context The application context.
     * @param isLoggedIn The boolean value indicating if the user is logged in.
     */
    public static void setLoggedIn(Context context, boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply(); // Use apply() for asynchronous saving
    }

    /**
     * Retrieves the user's login state from SharedPreferences.
     * @param context The application context.
     * @return true if the user is logged in, false otherwise.
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        // Returns the saved boolean value, or false if nothing is found.
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}