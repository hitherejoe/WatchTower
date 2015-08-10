package com.hitherejoe.watchtower.injection.module;

import android.content.Context;

import com.hitherejoe.watchtower.data.local.PreferencesHelper;
import com.hitherejoe.watchtower.data.remote.RetrofitHelper;
import com.hitherejoe.watchtower.data.remote.WatchTowerService;
import com.hitherejoe.watchtower.injection.scope.PerDataManager;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Provide dependencies to the DataManager, mainly Helper classes and Retrofit services.
 */
@Module
public class DataManagerModule {

    private final Context mContext;

    public DataManagerModule(Context context) {
        mContext = context;
    }

    @Provides
    @PerDataManager
    PreferencesHelper providePreferencesHelper() {
        return new PreferencesHelper(mContext);
    }

    @Provides
    @PerDataManager
    WatchTowerService provideRibotsService() {
        return new RetrofitHelper().newWatchTowerService(mContext);
    }

    @Provides
    @PerDataManager
    Scheduler provideSubscribeScheduler() {
        return Schedulers.io();
    }
}