package com.hitherejoe.watchtower.injection.component;

import com.hitherejoe.watchtower.injection.module.DataManagerTestModule;
import com.hitherejoe.watchtower.injection.scope.PerDataManager;

import dagger.Component;

@PerDataManager
@Component(dependencies = TestComponent.class, modules = DataManagerTestModule.class)
public interface DataManagerTestComponent extends DataManagerComponent {
}