package com.hitherejoe.watchtower;

import android.app.Application;
import android.content.Context;

import com.hitherejoe.watchtower.data.BusEvent;
import com.hitherejoe.watchtower.injection.component.ApplicationComponent;
import com.hitherejoe.watchtower.injection.component.DaggerApplicationComponent;
import com.hitherejoe.watchtower.injection.module.ApplicationModule;
import com.hitherejoe.watchtower.ui.activity.AuthActivity;
import com.squareup.otto.Subscribe;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class WatchTowerApplication extends Application {

    ApplicationComponent mApplicationComponent;
    private CompositeSubscription mSubscriptions;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        mSubscriptions = new CompositeSubscription();
        getComponent().eventBus().register(this);
    }

    @Override
    public void onTerminate() {
        getComponent().eventBus().unregister(this);
        super.onTerminate();
    }

    @Subscribe
    public void onAuthenticationError(BusEvent.AuthenticationError event) {
        mSubscriptions.add(getComponent().dataManager().clearUserCredentials()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {
                        startAuthActivityWithDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("There was an error clearing user credentials " + e);
                        startAuthActivityWithDialog();
                    }

                    @Override
                    public void onNext(Void aVoid) {
                    }
                }));
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

    private void startAuthActivityWithDialog() {
        startActivity(AuthActivity.getStartIntent(this, true));
    }

}
