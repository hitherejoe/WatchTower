package com.hitherejoe.watchtower.util;

import android.content.Context;

import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.data.remote.WatchTowerService;
import com.hitherejoe.watchtower.injection.component.DaggerDataManagerTestComponent;
import com.hitherejoe.watchtower.injection.component.TestComponent;
import com.hitherejoe.watchtower.injection.module.DataManagerTestModule;

/**
 * Extension of DataManager to be used on a testing environment.
 * It uses DataManagerTestComponent to inject dependencies that are different to the
 * normal runtime ones. e.g. mock objects etc.
 * It also exposes some helpers like the DatabaseHelper or the Retrofit service that are helpful
 * during testing.
 */
public class TestDataManager extends DataManager {

    public TestDataManager(Context context) {
        super(context);
    }

    @Override
    protected void injectDependencies(Context context) {
        TestComponent testComponent = (TestComponent)
                WatchTowerApplication.get(context).getComponent();
        DaggerDataManagerTestComponent.builder()
                .testComponent(testComponent)
                .dataManagerTestModule(new DataManagerTestModule(context))
                .build()
                .inject(this);
    }

    public WatchTowerService getWatchTowerService() {
        return mWatchTowerService;
    }

}