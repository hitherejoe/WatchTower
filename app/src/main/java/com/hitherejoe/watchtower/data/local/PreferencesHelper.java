package com.hitherejoe.watchtower.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

    private static SharedPreferences mPref;

    private static final String PREF_FILE_NAME = "watchtower_pref_file";
    private static final String PREF_KEY_TOKEN = "key_access_token";
    private static final String PREF_KEY_USER = "key_user";


    public PreferencesHelper(Context context) {
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void setUser(String user) {
        mPref.edit().putString(PREF_KEY_USER, user).apply();
    }

    public void saveToken(String token) {
        mPref.edit().putString(PREF_KEY_TOKEN, token).apply();
    }

    public String getUser() {
        return mPref.getString(PREF_KEY_USER, null);
    }
    public String getToken() {
        return mPref.getString(PREF_KEY_TOKEN, null);
    }

    public void clear() {
        mPref.edit().clear().apply();
    }

}
