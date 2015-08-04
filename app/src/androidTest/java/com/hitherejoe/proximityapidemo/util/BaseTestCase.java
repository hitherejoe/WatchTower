package com.hitherejoe.proximityapidemo.util;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.hitherejoe.proximityapidemo.android.ProximityApiApplication;
import com.hitherejoe.proximityapidemo.android.data.remote.ProximityApiService;

import rx.schedulers.Schedulers;

import static org.mockito.Mockito.mock;

public class BaseTestCase<T extends Activity> extends ActivityInstrumentationTestCase2<T> {

    protected ProximityApiService mProximityApiService;

    public BaseTestCase(Class<T> cls) {
        super(cls);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ProximityApiApplication.get().getDataManager().getPreferencesHelper().clear();
        mProximityApiService = mock(ProximityApiService.class);
        ProximityApiApplication.get().getDataManager().setAndroidBoilerplateService(mProximityApiService);
        ProximityApiApplication.get().getDataManager().setScheduler(Schedulers.immediate());
    }

}