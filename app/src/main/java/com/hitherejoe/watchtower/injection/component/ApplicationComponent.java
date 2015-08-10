package com.hitherejoe.watchtower.injection.component;

import android.app.Application;

import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.injection.module.ApplicationModule;
import com.hitherejoe.watchtower.ui.activity.MainActivity;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(MainActivity mainActivity);

    Application application();
    DataManager dataManager();
    Bus eventBus();
}