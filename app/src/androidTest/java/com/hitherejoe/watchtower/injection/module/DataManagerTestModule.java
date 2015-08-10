package com.hitherejoe.watchtower.injection.module;

import android.content.Context;

import com.hitherejoe.watchtower.data.local.PreferencesHelper;
import com.hitherejoe.watchtower.data.remote.WatchTowerService;
import com.hitherejoe.watchtower.injection.scope.PerDataManager;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import static org.mockito.Mockito.mock;

/**
 * Provides dependencies for an app running on a testing environment
 * This allows injecting mocks if necessary
 */
@Module
public class DataManagerTestModule {

    private final Context mContext;

    public DataManagerTestModule(Context context) {
        mContext = context;
    }

    @Provides
    @PerDataManager
    PreferencesHelper providePreferencesHelper() {
        return new PreferencesHelper(mContext);
    }

    @Provides
    @PerDataManager
    WatchTowerService provideWatchTowerService() {
        return mock(WatchTowerService.class);
    }

    @Provides
    @PerDataManager
    Scheduler provideSubscribeScheduler() {
        return Schedulers.immediate();
    }
}
