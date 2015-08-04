package com.hitherejoe.proximityapidemo.android.data;

import android.content.Context;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.hitherejoe.proximityapidemo.android.data.local.PreferencesHelper;
import com.hitherejoe.proximityapidemo.android.data.model.Attachment;
import com.hitherejoe.proximityapidemo.android.data.model.Beacon;
import com.hitherejoe.proximityapidemo.android.data.model.Diagnostics;
import com.hitherejoe.proximityapidemo.android.data.remote.ProximityApiService;
import com.hitherejoe.proximityapidemo.android.data.remote.RetrofitHelper;
import com.squareup.otto.Bus;

import java.io.IOException;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func1;

public class DataManager {

    private ProximityApiService mProximityApiService;
    private PreferencesHelper mPreferencesHelper;
    private Scheduler mScheduler;
    private Bus mEventBus;

    public DataManager(Context context, Scheduler scheduler) {
        mProximityApiService = new RetrofitHelper().setupProximityApiService();
        mPreferencesHelper = new PreferencesHelper(context);
        mScheduler = scheduler;
        mEventBus = new Bus();
    }

    public void setAndroidBoilerplateService(ProximityApiService proximityApiService) {
        mProximityApiService = proximityApiService;
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
        return mProximityApiService.registerBeacon(beacon);
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
        return mProximityApiService.getBeacons()
                .flatMapIterable(new Func1<ProximityApiService.BeaconsResponse, Iterable<? extends Beacon>>() {
                    @Override
                    public Iterable<? extends Beacon> call(ProximityApiService.BeaconsResponse beaconsResponse) {
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
        return mProximityApiService.updateBeacon(beaconName, beacon).flatMap(new Func1<Beacon, Observable<Beacon>>() {
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
                observable = mProximityApiService.activateBeacon(name);
                break;
            case INACTIVE:
                observable = mProximityApiService.deactivateBeacon(name);
                break;
            case DECOMMISSIONED:
                observable = mProximityApiService.decomissionBeacon(name);
                break;
            case STATUS_UNSPECIFIED:
                observable = mProximityApiService.deactivateBeacon(name);
                break;
        }
        if (observable != null) {
            return observable.flatMap(new Func1<Beacon, Observable<Beacon>>() {
                @Override
                public Observable<Beacon> call(Beacon beacon) {
                    return mProximityApiService.getBeacon(beaconBeacon.beaconName);
                }
            });
        }
        return Observable.just(beaconBeacon);
    }

    public Observable<Diagnostics> getDiagnostics(String beaconName) {
        return mProximityApiService.beaconDiagnostics(beaconName);
    }

    public Observable<Attachment> createAttachment(String beaconName, Attachment attachment) {
        return mProximityApiService.createAttachment(beaconName, attachment);
    }

    public Observable<Void> deleteAttachment(String attachmentName) {
        return mProximityApiService.deleteAttachment(attachmentName);
    }

    public Observable<ProximityApiService.AttachmentResponse> getAttachments(String beaconName, String namespaceType) {
        return mProximityApiService.getAttachments(beaconName, namespaceType);
    }

    public Observable<ProximityApiService.NamespacesResponse> getNamespaces() {
        return mProximityApiService.getNamespaces();
    }

    public Observable<ProximityApiService.AttachmentResponse> deleteBatchAttachments(final String beaconName) {
        return mProximityApiService.deleteBatchAttachments(beaconName).flatMap(new Func1<Void, Observable<ProximityApiService.AttachmentResponse>>() {
            @Override
            public Observable<ProximityApiService.AttachmentResponse> call(Void aVoid) {
                return getAttachments(beaconName, null);
            }
        });
    }

}
