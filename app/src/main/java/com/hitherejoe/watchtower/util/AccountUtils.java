package com.hitherejoe.watchtower.util;

import android.accounts.AccountManager;
import android.content.Context;

import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.data.local.PreferencesHelper;

public class AccountUtils {

    public static boolean isUserAuthenticated(Context context) {
        PreferencesHelper preferencesHelper = WatchTowerApplication.get(context).getComponent().dataManager().getPreferencesHelper();
        return preferencesHelper.getUser() != null && preferencesHelper.getToken() != null;
    }

    public static void invalidateToken(Context context) {
        DataManager dataManager =  WatchTowerApplication.get(context).getComponent().dataManager();
        AccountManager accountManager = AccountManager.get(context);
        accountManager.invalidateAuthToken("com.google", dataManager.getPreferencesHelper().getToken());
        dataManager.getPreferencesHelper().saveToken(null);
    }

}
