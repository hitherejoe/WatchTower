package com.hitherejoe.watchtower;

import android.app.Application;
import android.content.Context;

import com.hitherejoe.watchtower.data.DataManager;
import com.squareup.otto.Bus;

import rx.schedulers.Schedulers;
import timber.log.Timber;

public class WatchTowerApplication extends Application {

    private DataManager mDataManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mDataManager = new DataManager(this, Schedulers.io());
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
    }

    public static WatchTowerApplication get(Context context) {
        return (WatchTowerApplication) context.getApplicationContext();
    }

    public DataManager getDataManager() { return mDataManager; }

    public Bus getBus() {
        return mDataManager.getBus();
    }
}
