package com.hitherejoe.watchtower;


import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.data.local.PreferencesHelper;
import com.hitherejoe.watchtower.data.model.Attachment;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.model.Namespace;
import com.hitherejoe.watchtower.data.remote.WatchTowerService;
import com.hitherejoe.watchtower.util.DefaultConfig;
import com.hitherejoe.watchtower.util.MockModelsUtil;
import com.squareup.otto.Bus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.hitherejoe.watchtower.util.RxAssertions.subscribeAssertingThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK)
public class DataManagerTest {

    private DataManager mDataManager;
    private WatchTowerService mMockWatchTowerService;

    @Before
    public void setUp() {
        mMockWatchTowerService = mock(WatchTowerService.class);
        Bus mMockBus = mock(Bus.class);
        PreferencesHelper mPreferencesHelper = new PreferencesHelper(RuntimeEnvironment.application);
        mDataManager = new DataManager(mMockWatchTowerService,
                mMockBus,
                mPreferencesHelper,
                Schedulers.immediate());
    }

    @Test
    public void shouldRegisterBeacon() {
        Beacon unregisteredBeacon = MockModelsUtil.createMockUnregisteredBeacon();
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();
        when(mMockWatchTowerService.registerBeacon(unregisteredBeacon)).thenReturn(Observable.just(registeredBeacon));
        subscribeAssertingThat(mDataManager.registerBeacon(unregisteredBeacon))
                .emits(registeredBeacon);
    }

    @Test
    public void shouldGetBeacons() {
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();
        Beacon registeredBeaconTwo = MockModelsUtil.createMockRegisteredBeacon();
        registeredBeaconTwo.beaconName = "FUCK";
        WatchTowerService.BeaconsResponse beaconsResponse = new WatchTowerService.BeaconsResponse();
        beaconsResponse.beacons = new ArrayList<>();
        beaconsResponse.beacons.add(registeredBeacon);
        beaconsResponse.beacons.add(registeredBeaconTwo);
        when(mMockWatchTowerService.getBeacons()).thenReturn(Observable.just(beaconsResponse));

        subscribeAssertingThat(mDataManager.getBeacons())
                .emits(beaconsResponse.beacons);
    }

    @Test
    public void shouldUpdateBeacon() {
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();
        Beacon updatedBeacon = MockModelsUtil.createMockRegisteredBeacon();
        updatedBeacon.description = "Desc";

        when(mMockWatchTowerService.updateBeacon(registeredBeacon.beaconName, updatedBeacon))
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

        when(mMockWatchTowerService.decomissionBeacon(decommissionedBeacon.beaconName))
                .thenReturn(Observable.just(decommissionedBeacon));
        when(mMockWatchTowerService.getBeacon(decommissionedBeacon.beaconName))
                .thenReturn(Observable.just(decommissionedBeacon));
        subscribeAssertingThat(mDataManager.setBeaconStatus(decommissionedBeacon, Beacon.Status.DECOMMISSIONED))
                .emits(decommissionedBeacon);

        Beacon inactiveBeacon = MockModelsUtil.createMockRegisteredBeacon();
        inactiveBeacon.status = Beacon.Status.INACTIVE;

        when(mMockWatchTowerService.deactivateBeacon(inactiveBeacon.beaconName))
                .thenReturn(Observable.just(inactiveBeacon));
        when(mMockWatchTowerService.getBeacon(inactiveBeacon.beaconName))
                .thenReturn(Observable.just(inactiveBeacon));
        subscribeAssertingThat(mDataManager.setBeaconStatus(inactiveBeacon, Beacon.Status.INACTIVE))
                .emits(inactiveBeacon);

        Beacon activeBeacon = MockModelsUtil.createMockRegisteredBeacon();
        activeBeacon.status = Beacon.Status.ACTIVE;

        when(mMockWatchTowerService.activateBeacon(activeBeacon.beaconName))
                .thenReturn(Observable.just(activeBeacon));
        when(mMockWatchTowerService.getBeacon(activeBeacon.beaconName))
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

        when(mMockWatchTowerService.createAttachment(registeredBeacon.beaconName, unRegisteredAttachment))
                .thenReturn(Observable.just(registeredAttachment));
        subscribeAssertingThat(mDataManager.createAttachment(registeredBeacon.beaconName, unRegisteredAttachment))
                .emits(registeredAttachment);
    }

    @Test
    public void shouldDeleteAttachment() {
        Attachment registeredAttachment = MockModelsUtil.createMockAttachment();
        registeredAttachment.attachmentName = "attachmentName";

        when(mMockWatchTowerService.deleteAttachment(registeredAttachment.attachmentName))
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

        WatchTowerService.AttachmentResponse attachmentResponse = new WatchTowerService.AttachmentResponse();
        attachmentResponse.attachments = attachments;

        when(mMockWatchTowerService.getAttachments(any(String.class), any(String.class)))
                .thenReturn(Observable.just(attachmentResponse));

        subscribeAssertingThat(mDataManager.getAttachments(registeredBeacon.beaconName, null))
                .emits(attachmentResponse);
    }

    @Test
    public void getNamespaces() {
        Namespace namespace = MockModelsUtil.createMockNamespace();
        List<Namespace> namespaces = new ArrayList<>();
        namespaces.add(namespace);

        WatchTowerService.NamespacesResponse namespacesResponse = new WatchTowerService.NamespacesResponse();
        namespacesResponse.namespaces = namespaces;

        when(mMockWatchTowerService.getNamespaces())
                .thenReturn(Observable.just(namespacesResponse));

        subscribeAssertingThat(mDataManager.getNamespaces())
                .emits(namespacesResponse);
    }

    @Test
    public void shouldDeleteBatchAttachments() {
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();

        when(mMockWatchTowerService.deleteBatchAttachments(anyString(), anyString()))
                .thenReturn(Observable.<Void>empty());

        subscribeAssertingThat(mDataManager.deleteBatchAttachments(registeredBeacon.beaconName, null))
                .completesSuccessfully();
    }

}
