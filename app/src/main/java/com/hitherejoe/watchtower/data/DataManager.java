package com.hitherejoe.watchtower.data;

import android.content.Context;

import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.local.PreferencesHelper;
import com.hitherejoe.watchtower.data.model.Attachment;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.model.Diagnostics;
import com.hitherejoe.watchtower.data.remote.WatchTowerService;
import com.hitherejoe.watchtower.injection.component.DaggerDataManagerComponent;
import com.hitherejoe.watchtower.injection.module.DataManagerModule;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func1;

public class DataManager {

    @Inject protected WatchTowerService mWatchTowerService;
    @Inject protected PreferencesHelper mPreferencesHelper;
    @Inject protected Scheduler mSubscribeScheduler;
    @Inject protected Bus mEventBus;

    public DataManager(Context context) {
        injectDependencies(context);
    }

    /* This constructor is provided so we can set up a DataManager with mocks from unit test.
     * At the moment this is not possible to do with Dagger because the Gradle APT plugin doesn't
     * work for the unit test variant, plus Dagger 2 doesn't provide a nice way of overriding
     * modules */
    public DataManager(WatchTowerService watchTowerService,
                       Bus eventBus,
                       PreferencesHelper preferencesHelper,
                       Scheduler subscribeScheduler) {
        mWatchTowerService = watchTowerService;
        mEventBus = eventBus;
        mPreferencesHelper = preferencesHelper;
        mSubscribeScheduler = subscribeScheduler;
    }

    protected void injectDependencies(Context context) {
        DaggerDataManagerComponent.builder()
                .applicationComponent(WatchTowerApplication.get(context).getComponent())
                .dataManagerModule(new DataManagerModule(context))
                .build()
                .inject(this);
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public Scheduler getScheduler() {
        return mSubscribeScheduler;
    }

    public Bus getBus() {
        return mEventBus;
    }

    public Observable<Void> clearUserCredentials() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                mPreferencesHelper.clear();
                subscriber.onCompleted();
            }
        });
    }

    public Observable<Beacon> registerBeacon(Beacon beacon) {
        return mWatchTowerService.registerBeacon(beacon);
    }

    public Observable<Beacon> getBeacons() {
        return mWatchTowerService.getBeacons()
                .flatMapIterable(new Func1<WatchTowerService.BeaconsResponse, Iterable<? extends Beacon>>() {
                    @Override
                    public Iterable<? extends Beacon> call(WatchTowerService.BeaconsResponse beaconsResponse) {
                        return beaconsResponse.beacons;
                    }
                }).flatMap(new Func1<Beacon, Observable<Beacon>>() {
                    @Override
                    public Observable<Beacon> call(Beacon beacon) {
                        return Observable.just(beacon);
                    }
                });
    }

    public Observable<Beacon> updateBeacon(String beaconName, Beacon beacon, final boolean hasStatusChanges, final Beacon.Status status) {
        return mWatchTowerService.updateBeacon(beaconName, beacon).flatMap(new Func1<Beacon, Observable<Beacon>>() {
            @Override
            public Observable<Beacon> call(Beacon beacon) {
                if (hasStatusChanges) return setBeaconStatus(beacon, status);
                return Observable.just(beacon);
            }
        });
    }

    public Observable<Beacon> setBeaconStatus(Beacon beacon, Beacon.Status status) {
        String name = beacon.beaconName;
        Observable<Beacon> statusObservable = null;
        switch (status) {
            case ACTIVE:
                statusObservable = mWatchTowerService.activateBeacon(name);
                break;
            case INACTIVE:
                statusObservable = mWatchTowerService.deactivateBeacon(name);
                break;
            case DECOMMISSIONED:
                statusObservable = mWatchTowerService.decomissionBeacon(name);
                break;
            case STATUS_UNSPECIFIED:
                statusObservable = mWatchTowerService.deactivateBeacon(name);
                break;
        }
        if (statusObservable != null) {
            return statusObservable.flatMap(new Func1<Beacon, Observable<Beacon>>() {
                @Override
                public Observable<Beacon> call(Beacon updateBeacon) {
                    return mWatchTowerService.getBeacon(updateBeacon.beaconName);
                }
            });
        }
        return Observable.just(beacon);
    }

    public Observable<Diagnostics> getDiagnostics(String beaconName) {
        return mWatchTowerService.beaconDiagnostics(beaconName);
    }

    public Observable<Attachment> createAttachment(String beaconName, Attachment attachment) {
        return mWatchTowerService.createAttachment(beaconName, attachment);
    }

    public Observable<Void> deleteAttachment(String attachmentName) {
        return mWatchTowerService.deleteAttachment(attachmentName);
    }

    public Observable<WatchTowerService.AttachmentResponse> getAttachments(String beaconName, String namespaceType) {
        return mWatchTowerService.getAttachments(beaconName, namespaceType);
    }

    public Observable<WatchTowerService.NamespacesResponse> getNamespaces() {
        return mWatchTowerService.getNamespaces();
    }

    public Observable<WatchTowerService.AttachmentResponse> deleteBatchAttachments(final String beaconName, String type) {
        return mWatchTowerService.deleteBatchAttachments(beaconName, type).flatMap(new Func1<Void, Observable<WatchTowerService.AttachmentResponse>>() {
            @Override
            public Observable<WatchTowerService.AttachmentResponse> call(Void aVoid) {
                return getAttachments(beaconName, null);
            }
        });
    }

}
