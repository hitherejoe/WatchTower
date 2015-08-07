package com.hitherejoe.watchtower.data;

import android.content.Context;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.hitherejoe.watchtower.data.local.PreferencesHelper;
import com.hitherejoe.watchtower.data.model.Attachment;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.model.Diagnostics;
import com.hitherejoe.watchtower.data.remote.WatchTowerService;
import com.hitherejoe.watchtower.data.remote.RetrofitHelper;
import com.squareup.otto.Bus;

import java.io.IOException;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func1;

public class DataManager {

    private WatchTowerService mWatchTowerService;
    private PreferencesHelper mPreferencesHelper;
    private Scheduler mScheduler;
    private Bus mEventBus;

    public DataManager(Context context, Scheduler scheduler) {
        mWatchTowerService = new RetrofitHelper().setupProximityApiService(context);
        mPreferencesHelper = new PreferencesHelper(context);
        mScheduler = scheduler;
        mEventBus = new Bus();
    }

    public void setWatchTowerService(WatchTowerService watchTowerService) {
        mWatchTowerService = watchTowerService;
    }

    public void setScheduler(Scheduler scheduler) {
        mScheduler = scheduler;
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public Scheduler getScheduler() {
        return mScheduler;
    }

    public Bus getBus() {
        return mEventBus;
    }

    public Observable<Beacon> registerBeacon(Beacon beacon) {
        return mWatchTowerService.registerBeacon(beacon);
    }

    public Observable<String> getAccessToken(final Context context, final String accountName, final String scopes, final boolean cacheToken) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    String token = GoogleAuthUtil.getToken(context, accountName, scopes);
                    if (!cacheToken) GoogleAuthUtil.clearToken(context, token);
                    subscriber.onNext(token);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                } catch (UserRecoverableAuthException e) {
                    subscriber.onError(e);
                } catch (GoogleAuthException e) {
                    subscriber.onError(e);
                }
            }
        });
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

    public Observable<Beacon> setBeaconStatus(final Beacon beaconBeacon, Beacon.Status status) {
        String name = beaconBeacon.beaconName;
        Observable<Beacon> observable = null;
        switch (status) {
            case ACTIVE:
                observable = mWatchTowerService.activateBeacon(name);
                break;
            case INACTIVE:
                observable = mWatchTowerService.deactivateBeacon(name);
                break;
            case DECOMMISSIONED:
                observable = mWatchTowerService.decomissionBeacon(name);
                break;
            case STATUS_UNSPECIFIED:
                observable = mWatchTowerService.deactivateBeacon(name);
                break;
        }
        if (observable != null) {
            return observable.flatMap(new Func1<Beacon, Observable<Beacon>>() {
                @Override
                public Observable<Beacon> call(Beacon beacon) {
                    return mWatchTowerService.getBeacon(beaconBeacon.beaconName);
                }
            });
        }
        return Observable.just(beaconBeacon);
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
