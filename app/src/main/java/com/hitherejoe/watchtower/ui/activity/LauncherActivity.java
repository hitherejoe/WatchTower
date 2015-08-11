package com.hitherejoe.watchtower.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.hitherejoe.watchtower.util.AccountUtils;

public class LauncherActivity extends Activity {

    public LauncherActivity() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(AccountUtils.isUserAuthenticated(this)
                ? MainActivity.getStartIntent(this)
                : AuthActivity.getStartIntent(this, false));
        finish();
    }
}