package com.hitherejoe.watchtower.util;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.remote.WatchTowerService;

import rx.schedulers.Schedulers;

import static org.mockito.Mockito.mock;

public class BaseTestCase<T extends Activity> extends ActivityInstrumentationTestCase2<T> {

    protected WatchTowerService mWatchTowerService;

    public BaseTestCase(Class<T> cls) {
        super(cls);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        WatchTowerApplication.get().getDataManager().getPreferencesHelper().clear();
        mWatchTowerService = mock(WatchTowerService.class);
        WatchTowerApplication.get().getDataManager().setWatchTowerService(mWatchTowerService);
        WatchTowerApplication.get().getDataManager().setScheduler(Schedulers.immediate());
    }

}