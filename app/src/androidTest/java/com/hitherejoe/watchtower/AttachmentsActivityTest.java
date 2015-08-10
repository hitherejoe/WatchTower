package com.hitherejoe.watchtower;


import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hitherejoe.watchtower.data.model.Attachment;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.model.Namespace;
import com.hitherejoe.watchtower.data.remote.WatchTowerService;
import com.hitherejoe.watchtower.injection.TestComponentRule;
import com.hitherejoe.watchtower.ui.activity.AttachmentsActivity;
import com.hitherejoe.watchtower.util.MockModelsUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openContextualActionModeOverflowMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class AttachmentsActivityTest {

    @Rule
    public final ActivityTestRule<AttachmentsActivity> main =
            new ActivityTestRule<>(AttachmentsActivity.class, false, false);

    @Rule
    public final TestComponentRule component = new TestComponentRule();

    @Test
    public void testAttachmentsDisplayed() {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        List<Attachment> attachments = MockModelsUtil.createMockListOfAttachments(beacon.beaconName, 10);
        stubMockAttachments(beacon.beaconName, attachments);

        Intent i = new Intent(AttachmentsActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), beacon));
        main.launchActivity(i);

        checkAttachmentsDisplayOnRecyclerView(attachments);
    }

    @Test
    public void testDeleteAttachment() {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        List<Attachment> attachments = MockModelsUtil.createMockListOfAttachments(beacon.beaconName, 1);
        stubMockAttachments(beacon.beaconName, attachments);

        when(component.getMockWatchTowerService().deleteAttachment(attachments.get(0).attachmentName))
                .thenReturn(Observable.<Void>empty());

        Intent i = new Intent(AttachmentsActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), beacon));
        main.launchActivity(i);

        onView(withId(R.id.text_delete))
                .perform(click());
        onView(withText(R.string.text_no_attachments))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEmptyAttachmentsFeed() {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        stubMockAttachments(beacon.beaconName, new ArrayList<Attachment>());
        Intent i = new Intent(AttachmentsActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), beacon));
        main.launchActivity(i);

        onView(withText(R.string.text_no_attachments))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testAddAttachmentActivityStarted() {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        stubMockAttachments(beacon.beaconName, new ArrayList<Attachment>());
        List<Namespace> namespaces = MockModelsUtil.createMockListOfNamespaces(1);
        WatchTowerService.NamespacesResponse namespacesResponse = new WatchTowerService.NamespacesResponse();
        namespacesResponse.namespaces = namespaces;

        when(component.getMockWatchTowerService().getNamespaces())
                .thenReturn(Observable.just(namespacesResponse));

        Intent i = new Intent(AttachmentsActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), beacon));
        main.launchActivity(i);

        onView(withId(R.id.fab_add))
                .perform(click());
        onView(withId(R.id.spinner_namespace))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testDeleteAllAttachments() {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        List<Attachment> attachments = MockModelsUtil.createMockListOfAttachments(beacon.beaconName, 10);
        stubMockAttachments(beacon.beaconName, attachments);

        WatchTowerService.AttachmentResponse attachmentResponse = new WatchTowerService.AttachmentResponse();
        attachmentResponse.attachments = new ArrayList<>();

        when(component.getMockWatchTowerService().deleteBatchAttachments(beacon.beaconName, null))
                .thenReturn(Observable.<Void>empty());

        Intent i = new Intent(AttachmentsActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), beacon));
        main.launchActivity(i);

        when(component.getMockWatchTowerService().getAttachments(beacon.beaconName, null))
                .thenReturn(Observable.just(attachmentResponse));

        openContextualActionModeOverflowMenu();
        onView(withText(R.string.action_delete_all))
                .perform(click());
    }

    @Test
    public void testAddAttachmentValidInput() {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        Attachment attachment = MockModelsUtil.createMockAttachment();
        WatchTowerService.NamespacesResponse namespacesResponse = new WatchTowerService.NamespacesResponse();
        namespacesResponse.namespaces = MockModelsUtil.createMockListOfNamespaces(1);

        when(component.getMockWatchTowerService().getNamespaces())
                .thenReturn(Observable.just(namespacesResponse));

        List<Attachment> attachments = MockModelsUtil.createMockListOfAttachments(beacon.beaconName, 10);
        stubMockAttachments(beacon.beaconName, attachments);

        WatchTowerService.AttachmentResponse attachmentResponse = new WatchTowerService.AttachmentResponse();
        attachmentResponse.attachments = new ArrayList<>();

        when(component.getMockWatchTowerService().deleteBatchAttachments(beacon.beaconName, null))
                .thenReturn(Observable.<Void>empty());

        when(component.getMockWatchTowerService().createAttachment(anyString(), any(Attachment.class)))
                .thenReturn(Observable.just(attachment));

        Intent i = new Intent(AttachmentsActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), beacon));
        main.launchActivity(i);

        onView(withId(R.id.fab_add)).perform(click());

        onData(allOf(is(instanceOf(String.class)), is(namespacesResponse.namespaces.get(0).namespaceName)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_data))
                .perform(typeText("Data"));
        onView(withId(R.id.action_done))
                .perform(click());
        onView(withId(R.id.fab_add)).check(matches(isDisplayed()));
    }

    private void checkAttachmentsDisplayOnRecyclerView(List<Attachment> beaconsToCheck) {
        for (int i = 0; i < beaconsToCheck.size(); i++) {
            onView(withId(R.id.recycler_attachments))
                    .perform(RecyclerViewActions.scrollToPosition(i));
            checkPostDisplays(beaconsToCheck.get(i));
        }
    }

    private void checkPostDisplays(Attachment attachment) {
        onView(withText(attachment.attachmentName))
                .check(matches(isDisplayed()));
    }

    private void stubMockAttachments(String beaconName, List<Attachment> mockAttachments) {
        WatchTowerService.AttachmentResponse attachmentResponse = new WatchTowerService.AttachmentResponse();
        attachmentResponse.attachments = mockAttachments;
        when(component.getMockWatchTowerService().getAttachments(beaconName, null))
                .thenReturn(Observable.just(attachmentResponse));
    }
}