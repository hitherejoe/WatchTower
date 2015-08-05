package com.hitherejoe.watchtower.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

    private static SharedPreferences mPref;

    public static final String PREF_FILE_NAME = "android_boilerplate_pref_file";
    public static final String PREF_KEY_TOKEN = "key_token";


    public PreferencesHelper(Context context) {
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        mPref.edit().putString(PREF_KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return mPref.getString(PREF_KEY_TOKEN, null);
    }

    public void clear() {
        mPref.edit().clear().apply();
    }

}
