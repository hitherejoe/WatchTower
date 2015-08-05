package com.hitherejoe.watchtower.util;

import android.accounts.AccountManager;
import android.content.Context;

public class AccountUtils {

    public static boolean isUserAuthenticated(Context context, String accountName) {
        AccountManager accountManager = AccountManager.get(context);
        return false;
    }
}
