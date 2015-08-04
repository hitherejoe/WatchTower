package com.hitherejoe.proximityapidemo.android;


import com.hitherejoe.proximityapidemo.android.data.DataManager;
import com.hitherejoe.proximityapidemo.android.data.model.Attachment;
import com.hitherejoe.proximityapidemo.android.data.model.Beacon;
import com.hitherejoe.proximityapidemo.android.data.model.Namespace;
import com.hitherejoe.proximityapidemo.android.data.remote.ProximityApiService;
import com.hitherejoe.proximityapidemo.android.util.DefaultConfig;
import com.hitherejoe.proximityapidemo.android.util.MockModelsUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.hitherejoe.proximityapidemo.android.util.RxAssertions.subscribeAssertingThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = DefaultConfig.EMULATE_SDK)
public class DataManagerTest {

    private DataManager mDataManager;
    private ProximityApiService mProximityApiService;

    @Before
    public void setUp() {
        mDataManager = new DataManager(RuntimeEnvironment.application, Schedulers.immediate());
        mProximityApiService = mock(ProximityApiService.class);
        mDataManager.setAndroidBoilerplateService(mProximityApiService);
    }

    @Test
    public void shouldRegisterBeacon() {
        Beacon unregisteredBeacon = MockModelsUtil.createMockUnregisteredBeacon();
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();
        when(mProximityApiService.registerBeacon(unregisteredBeacon)).thenReturn(Observable.just(registeredBeacon));
        subscribeAssertingThat(mDataManager.registerBeacon(unregisteredBeacon))
                .emits(registeredBeacon);
    }

    @Test
    public void shouldGetBeacons() {
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();
        Beacon registeredBeaconTwo = MockModelsUtil.createMockRegisteredBeacon();
        registeredBeaconTwo.beaconName = "FUCK";
        ProximityApiService.BeaconsResponse beaconsResponse = new ProximityApiService.BeaconsResponse();
        beaconsResponse.beacons = new ArrayList<>();
        beaconsResponse.beacons.add(registeredBeacon);
        beaconsResponse.beacons.add(registeredBeaconTwo);
        when(mProximityApiService.getBeacons()).thenReturn(Observable.just(beaconsResponse));

        subscribeAssertingThat(mDataManager.getBeacons())
                .emits(beaconsResponse.beacons);
    }

    @Test
    public void shouldUpdateBeacon() {
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();
        Beacon updatedBeacon = MockModelsUtil.createMockRegisteredBeacon();
        updatedBeacon.description = "Desc";

        when(mProximityApiService.updateBeacon(registeredBeacon.beaconName, updatedBeacon))
                .thenReturn(Observable.just(updatedBeacon));

        subscribeAssertingThat(mDataManager.updateBeacon(registeredBeacon.beaconName, updatedBeacon, false, Beacon.Status.ACTIVE))
                .emits(updatedBeacon);

        updatedBeacon.status = Beacon.Status.INACTIVE;
        subscribeAssertingThat(mDataManager.updateBeacon(updatedBeacon.beaconName, updatedBeacon, true, Beacon.Status.INACTIVE))
                .emits(updatedBeacon);
    }

    @Test
    public void shouldUpdateBeaconStatus() {
        Beacon decommissionedBeacon = MockModelsUtil.createMockRegisteredBeacon();
        decommissionedBeacon.status = Beacon.Status.DECOMMISSIONED;

        when(mProximityApiService.decomissionBeacon(decommissionedBeacon.beaconName))
                .thenReturn(Observable.just(decommissionedBeacon));
        when(mProximityApiService.getBeacon(decommissionedBeacon.beaconName))
                .thenReturn(Observable.just(decommissionedBeacon));
        subscribeAssertingThat(mDataManager.setBeaconStatus(decommissionedBeacon, Beacon.Status.DECOMMISSIONED))
                .emits(decommissionedBeacon);

        Beacon inactiveBeacon = MockModelsUtil.createMockRegisteredBeacon();
        inactiveBeacon.status = Beacon.Status.INACTIVE;

        when(mProximityApiService.deactivateBeacon(inactiveBeacon.beaconName))
                .thenReturn(Observable.just(inactiveBeacon));
        when(mProximityApiService.getBeacon(inactiveBeacon.beaconName))
                .thenReturn(Observable.just(inactiveBeacon));
        subscribeAssertingThat(mDataManager.setBeaconStatus(inactiveBeacon, Beacon.Status.INACTIVE))
                .emits(inactiveBeacon);

        Beacon activeBeacon = MockModelsUtil.createMockRegisteredBeacon();
        activeBeacon.status = Beacon.Status.ACTIVE;

        when(mProximityApiService.activateBeacon(activeBeacon.beaconName))
                .thenReturn(Observable.just(activeBeacon));
        when(mProximityApiService.getBeacon(activeBeacon.beaconName))
                .thenReturn(Observable.just(activeBeacon));
        subscribeAssertingThat(mDataManager.setBeaconStatus(activeBeacon, Beacon.Status.ACTIVE))
                .emits(activeBeacon);
    }

    @Test
    public void shouldCreateAttachment() {
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();
        Attachment unRegisteredAttachment = MockModelsUtil.createMockAttachment();
        Attachment registeredAttachment = MockModelsUtil.createMockAttachment();
        registeredAttachment.attachmentName = "attachmentName";

        when(mProximityApiService.createAttachment(registeredBeacon.beaconName, unRegisteredAttachment))
                .thenReturn(Observable.just(registeredAttachment));
        subscribeAssertingThat(mDataManager.createAttachment(registeredBeacon.beaconName, unRegisteredAttachment))
                .emits(registeredAttachment);
    }

    @Test
    public void shouldDeleteAttachment() {
        Attachment registeredAttachment = MockModelsUtil.createMockAttachment();
        registeredAttachment.attachmentName = "attachmentName";

        when(mProximityApiService.deleteAttachment(registeredAttachment.attachmentName))
                .thenReturn(Observable.<Void>empty());
        subscribeAssertingThat(mDataManager.deleteAttachment(registeredAttachment.attachmentName))
                .completesSuccessfully();
    }

    @Test
    public void shouldGetAttachments() {
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();
        Attachment registeredAttachmentOne = MockModelsUtil.createMockAttachment();
        registeredAttachmentOne.attachmentName = "attachmentNameOne";
        Attachment registeredAttachmentTwo = MockModelsUtil.createMockAttachment();
        registeredAttachmentTwo.attachmentName = "attachmentNameTwo";
        List<Attachment> attachments = new ArrayList<>();
        attachments.add(registeredAttachmentOne);
        attachments.add(registeredAttachmentTwo);

        ProximityApiService.AttachmentResponse attachmentResponse = new ProximityApiService.AttachmentResponse();
        attachmentResponse.attachments = attachments;

        when(mProximityApiService.getAttachments(any(String.class), any(String.class)))
                .thenReturn(Observable.just(attachmentResponse));

        subscribeAssertingThat(mDataManager.getAttachments(registeredBeacon.beaconName, null))
                .emits(attachmentResponse);
    }

    @Test
    public void getNamespaces() {
        Namespace namespace = MockModelsUtil.createMockNamespace();
        List<Namespace> namespaces = new ArrayList<>();
        namespaces.add(namespace);

        ProximityApiService.NamespacesResponse namespacesResponse = new ProximityApiService.NamespacesResponse();
        namespacesResponse.namespaces = namespaces;

        when(mProximityApiService.getNamespaces())
                .thenReturn(Observable.just(namespacesResponse));

        subscribeAssertingThat(mDataManager.getNamespaces())
                .emits(namespacesResponse);
    }

    @Test
    public void shouldDeleteBatchAttachments() {
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();

        when(mProximityApiService.deleteBatchAttachments(any(String.class)))
                .thenReturn(Observable.<Void>empty());

        subscribeAssertingThat(mDataManager.deleteBatchAttachments(registeredBeacon.beaconName, null))
                .completesSuccessfully();
    }

}
