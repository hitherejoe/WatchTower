package com.hitherejoe.proximityapidemo.android;

import android.app.Application;

import com.hitherejoe.proximityapidemo.android.data.DataManager;
import com.squareup.otto.Bus;

import rx.schedulers.Schedulers;

public class ProximityApiApplication extends Application {

    private static ProximityApiApplication sProximityApiApplication;
    private DataManager mDataManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sProximityApiApplication = this;
        mDataManager = new DataManager(this, Schedulers.io());
    }

    @Override
    public void onTerminate() {
        sProximityApiApplication = null;
        super.onTerminate();
    }

    public static ProximityApiApplication get() {
        return sProximityApiApplication;
    }

    public DataManager getDataManager() { return mDataManager; }

    public Bus getBus() {
        return mDataManager.getBus();
    }
}
