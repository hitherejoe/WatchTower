package com.hitherejoe.watchtower.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.local.PreferencesHelper;

public class LauncherActivity extends Activity {

    public LauncherActivity() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesHelper preferencesHelper = WatchTowerApplication.get(this).getComponent().dataManager().getPreferencesHelper();
        if (preferencesHelper.getToken() == null) {
            startActivity(AuthActivity.getStartIntent(this, false));
            finish();
        } else {
            startActivity(MainActivity.getStartIntent(this));
            finish();
        }
    }
}