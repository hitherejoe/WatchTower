package com.hitherejoe.proximityapidemo;


import android.content.Intent;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.hitherejoe.proximityapidemo.android.R;
import com.hitherejoe.proximityapidemo.android.data.model.Beacon;
import com.hitherejoe.proximityapidemo.android.data.model.Diagnostics;
import com.hitherejoe.proximityapidemo.android.data.remote.ProximityApiService;
import com.hitherejoe.proximityapidemo.android.ui.activity.MainActivity;
import com.hitherejoe.proximityapidemo.android.ui.activity.PropertiesActivity;
import com.hitherejoe.proximityapidemo.android.ui.fragment.PropertiesFragment;
import com.hitherejoe.proximityapidemo.android.util.MockModelsUtil;
import com.hitherejoe.proximityapidemo.util.BaseTestCase;

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
import static android.support.test.espresso.matcher.ViewMatchers.isFocusable;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class MainActivityTest extends BaseTestCase<MainActivity> {

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(mProximityApiService.beaconDiagnostics(anyString()))
                .thenReturn(Observable.<Diagnostics>empty());
    }

    public void testBeaconsShowAndAreScrollableInFeed() throws Exception {
        List<Beacon> mockBeacons = MockModelsUtil.createMockListOfBeacons(20);
        stubMockPosts(mockBeacons);
        getActivity();

        checkPostsDisplayOnRecyclerView(mockBeacons);
    }

    public void testClickOnCardAndNavigateToBeaconDetails() throws Exception {
        List<Beacon> mockBeacons = MockModelsUtil.createMockListOfBeacons(1);
        stubMockPosts(mockBeacons);
        getActivity();

        onView(withText(mockBeacons.get(0).beaconName))
                .perform(click());
        onView(withText(mockBeacons.get(0).beaconName))
                .check(matches(isDisplayed()));
    }

    public void testClickOnViewAndNavigateToBeaconDetails() throws Exception {
        List<Beacon> mockBeacons = MockModelsUtil.createMockListOfBeacons(1);
        stubMockPosts(mockBeacons);
        getActivity();

        onView(withText(R.string.text_view))
                .perform(click());
        onView(withText(mockBeacons.get(0).beaconName))
                .check(matches(isDisplayed()));
    }

    public void testClickOnAttachmentsAndNavigateToBeaconAttachments() throws Exception {
        List<Beacon> mockBeacons = MockModelsUtil.createMockListOfBeacons(1);
        stubMockPosts(mockBeacons);

        ProximityApiService.AttachmentResponse attachmentResponse = new ProximityApiService.AttachmentResponse();
        attachmentResponse.attachments = MockModelsUtil.createMockListOfAttachments(mockBeacons.get(0).beaconName, 1);
        when(mProximityApiService.getAttachments(mockBeacons.get(0).beaconName, null))
                .thenReturn(Observable.just(attachmentResponse));
        getActivity();

        onView(withId(R.id.text_attachments))
                .perform(click());
        onView(withText(attachmentResponse.attachments.get(0).attachmentName))
                .check(matches(isDisplayed()));
    }

    public void testEmptyPostsFeed() throws Exception {
        stubMockPosts(new ArrayList<Beacon>());
        getActivity();

        onView(withText(R.string.text_no_beacons))
                .check(matches(isDisplayed()));
    }

    public void testRegisterBeaconValidData() throws Exception {
        Beacon mockBeacon = MockModelsUtil.createMockUnregisteredBeacon();
        mockBeacon.status = Beacon.Status.ACTIVE;
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();

        when(mProximityApiService.registerBeacon(Matchers.any(Beacon.class)))
                .thenReturn(Observable.just(registeredBeacon));

        Intent i = new Intent(PropertiesActivity.getStartIntent(getInstrumentation().getContext(), PropertiesFragment.Mode.REGISTER));
        setActivityIntent(i);
        stubMockPosts(new ArrayList<Beacon>());
        getActivity();

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
        ProximityApiService.BeaconsResponse beaconsResponse = new ProximityApiService.BeaconsResponse();
        beaconsResponse.beacons = mockBeacons;
        when(mProximityApiService.getBeacons())
                .thenReturn(Observable.just(beaconsResponse));
    }
}