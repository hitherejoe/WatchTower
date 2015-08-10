package com.hitherejoe.watchtower;


import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.model.Diagnostics;
import com.hitherejoe.watchtower.data.remote.WatchTowerService;
import com.hitherejoe.watchtower.injection.TestComponentRule;
import com.hitherejoe.watchtower.ui.activity.MainActivity;
import com.hitherejoe.watchtower.ui.activity.PropertiesActivity;
import com.hitherejoe.watchtower.ui.fragment.PropertiesFragment;
import com.hitherejoe.watchtower.util.MockModelsUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public final ActivityTestRule<MainActivity> main =
            new ActivityTestRule<>(MainActivity.class, false, false);

    @Rule
    public final TestComponentRule component = new TestComponentRule();

    @Test
    public void testBeaconsShowAndAreScrollableInFeed() {
        List<Beacon> mockBeacons = MockModelsUtil.createMockListOfBeacons(20);
        stubMockPosts(mockBeacons);
        main.launchActivity(null);

        checkPostsDisplayOnRecyclerView(mockBeacons);
    }

    @Test
    public void testClickOnCardAndNavigateToBeaconDetails() {

        List<Beacon> mockBeacons = MockModelsUtil.createMockListOfBeacons(1);
        stubMockPosts(mockBeacons);

        String beaconName = mockBeacons.get(0).beaconName;
        Diagnostics diagnostics = MockModelsUtil.createMockDiagnostics(beaconName);
        when(component.getMockWatchTowerService().beaconDiagnostics(beaconName))
                .thenReturn(Observable.just(diagnostics));

        main.launchActivity(null);

        onView(withText(mockBeacons.get(0).beaconName))
                .perform(click());
        onView(withText(mockBeacons.get(0).beaconName))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testClickOnViewAndNavigateToBeaconDetails() {
        List<Beacon> mockBeacons = MockModelsUtil.createMockListOfBeacons(1);
        stubMockPosts(mockBeacons);

        String beaconName = mockBeacons.get(0).beaconName;
        Diagnostics diagnostics = MockModelsUtil.createMockDiagnostics(beaconName);
        when(component.getMockWatchTowerService().beaconDiagnostics(beaconName))
                .thenReturn(Observable.just(diagnostics));

        main.launchActivity(null);

        onView(withText(R.string.text_view))
                .perform(click());
        onView(withText(mockBeacons.get(0).beaconName))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testClickOnAttachmentsAndNavigateToBeaconAttachments() {
        List<Beacon> mockBeacons = MockModelsUtil.createMockListOfBeacons(1);
        stubMockPosts(mockBeacons);

        WatchTowerService.AttachmentResponse attachmentResponse = new WatchTowerService.AttachmentResponse();
        attachmentResponse.attachments = MockModelsUtil.createMockListOfAttachments(mockBeacons.get(0).beaconName, 1);
        when(component.getMockWatchTowerService().getAttachments(mockBeacons.get(0).beaconName, null))
                .thenReturn(Observable.just(attachmentResponse));
        main.launchActivity(null);

        onView(withId(R.id.text_attachments))
                .perform(click());
        onView(withText(attachmentResponse.attachments.get(0).attachmentName))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEmptyPostsFeed() {
        stubMockPosts(new ArrayList<Beacon>());
        main.launchActivity(null);

        onView(withText(R.string.text_no_beacons))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testRegisterBeaconValidData() {
        Beacon mockBeacon = MockModelsUtil.createMockUnregisteredBeacon();
        mockBeacon.status = Beacon.Status.ACTIVE;
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();

        when(component.getMockWatchTowerService().registerBeacon(Matchers.any(Beacon.class)))
                .thenReturn(Observable.just(registeredBeacon));

        Intent i = new Intent(PropertiesActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), PropertiesFragment.Mode.REGISTER));
        stubMockPosts(new ArrayList<Beacon>());
        main.launchActivity(i);

        onView(withId(R.id.fab_add)).perform(click());

        onView(withId(R.id.edit_text_advertised_id)).perform(typeText(mockBeacon.advertisedId.id));
        onView(withId(R.id.edit_text_description)).perform(typeText(mockBeacon.description));
        onView(withId(R.id.spinner_type)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("Eddystone"))).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.spinner_status)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("Active"))).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.spinner_stability)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("Mobile"))).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.edit_text_latitude)).perform(scrollTo(), typeText(String.valueOf(mockBeacon.latLng.latitude)));
        onView(withId(R.id.edit_text_longitude)).perform(scrollTo(), typeText(String.valueOf(mockBeacon.latLng.longitude)));
        onView(withId(R.id.edit_text_place_id)).perform(scrollTo(), typeText(mockBeacon.placeId));
        onView(withId(R.id.action_done)).perform(click());
        onView(withId(R.id.fab_add)).check(matches(isDisplayed()));
    }

    private void checkPostsDisplayOnRecyclerView(List<Beacon> beaconsToCheck) {
        for (int i = 0; i < beaconsToCheck.size(); i++) {
            onView(withId(R.id.recycler_beacons))
                    .perform(RecyclerViewActions.scrollToPosition(i));
            checkPostDisplays(beaconsToCheck.get(i));
        }
    }

    private void checkPostDisplays(Beacon beacon) {
        onView(withText(beacon.beaconName))
                .check(matches(isDisplayed()));
    }

    private void stubMockPosts(List<Beacon> mockBeacons) {
        WatchTowerService.BeaconsResponse beaconsResponse = new WatchTowerService.BeaconsResponse();
        beaconsResponse.beacons = mockBeacons;
        when(component.getMockWatchTowerService().getBeacons())
                .thenReturn(Observable.just(beaconsResponse));
    }
}