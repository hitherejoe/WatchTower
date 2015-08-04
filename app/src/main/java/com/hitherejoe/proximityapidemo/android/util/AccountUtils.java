package com.hitherejoe.proximityapidemo.android.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class AccountUtils {

    public static boolean isUserAuthenticated(Context context, String accountName) {
        AccountManager accountManager = AccountManager.get(context);
        return false;
    }
}
