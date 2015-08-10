package com.hitherejoe.watchtower;

import android.app.Application;
import android.content.Context;

import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.injection.component.ApplicationComponent;
import com.hitherejoe.watchtower.injection.component.DaggerApplicationComponent;
import com.hitherejoe.watchtower.injection.module.ApplicationModule;
import com.squareup.otto.Bus;

import rx.schedulers.Schedulers;
import timber.log.Timber;

public class WatchTowerApplication extends Application {

    ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public static WatchTowerApplication get(Context context) {
        return (WatchTowerApplication) context.getApplicationContext();
    }

    public ApplicationComponent getComponent() {
        return mApplicationComponent;
    }

    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        mApplicationComponent = applicationComponent;
    }

}
