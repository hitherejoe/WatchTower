package com.hitherejoe.watchtower.injection.component;

import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.injection.module.DataManagerModule;
import com.hitherejoe.watchtower.injection.scope.PerDataManager;

import dagger.Component;

@PerDataManager
@Component(dependencies = ApplicationComponent.class, modules = DataManagerModule.class)
public interface DataManagerComponent {

    void inject(DataManager dataManager);
}