package com.hitherejoe.watchtower;

import android.app.Application;

import com.hitherejoe.watchtower.data.DataManager;
import com.squareup.otto.Bus;

import rx.schedulers.Schedulers;

public class WatchTowerApplication extends Application {

    private static WatchTowerApplication sWatchTowerApplication;
    private DataManager mDataManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sWatchTowerApplication = this;
        mDataManager = new DataManager(this, Schedulers.io());
    }

    @Override
    public void onTerminate() {
        sWatchTowerApplication = null;
        super.onTerminate();
    }

    public static WatchTowerApplication get() {
        return sWatchTowerApplication;
    }

    public DataManager getDataManager() { return mDataManager; }

    public Bus getBus() {
        return mDataManager.getBus();
    }
}
