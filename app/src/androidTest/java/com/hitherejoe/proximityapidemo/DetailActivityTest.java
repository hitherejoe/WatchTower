package com.hitherejoe.proximityapidemo;


import android.content.Intent;

import com.hitherejoe.proximityapidemo.android.R;
import com.hitherejoe.proximityapidemo.android.data.model.Beacon;
import com.hitherejoe.proximityapidemo.android.data.model.Diagnostics;
import com.hitherejoe.proximityapidemo.android.ui.activity.AttachmentsActivity;
import com.hitherejoe.proximityapidemo.android.ui.activity.DetailActivity;
import com.hitherejoe.proximityapidemo.android.util.MockModelsUtil;
import com.hitherejoe.proximityapidemo.util.BaseTestCase;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isFocusable;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.when;

public class DetailActivityTest extends BaseTestCase<DetailActivity> {

    public DetailActivityTest() {
        super(DetailActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testNavBarFunctionality() throws Exception {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();

        Diagnostics diagnostics = new Diagnostics();
        diagnostics.alerts = new Diagnostics.Alert[0];
        diagnostics.beaconName = "";
        diagnostics.estimatedLowBatteryDate = null;

        when(mProximityApiService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));

        Intent i = new Intent(AttachmentsActivity.getStartIntent(getInstrumentation().getContext(), beacon));
        setActivityIntent(i);
        getActivity();

        onView(withText(R.string.fragment_title_properties)).check(matches(isDisplayed()));
        onView(withText(R.string.fragment_title_alerts)).check(matches(isDisplayed()));

        onView(withText(R.string.fragment_title_properties)).perform(click());
        onView(withId(R.id.text_title_beacon_name)).check(matches(isDisplayed()));

        onView(withText(R.string.fragment_title_alerts)).perform(click());
        onView(withId(R.id.text_title_beacon_name)).check(matches(not(isDisplayed())));
        onView(withId(R.id.text_battery_title)).check(matches(isDisplayed()));
    }

    public void testNavigateToUpdateBeaconActivity() throws Exception {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();

        Diagnostics diagnostics = new Diagnostics();
        diagnostics.alerts = new Diagnostics.Alert[0];
        diagnostics.beaconName = "";
        diagnostics.estimatedLowBatteryDate = null;

        when(mProximityApiService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));

        Intent i = new Intent(AttachmentsActivity.getStartIntent(getInstrumentation().getContext(), beacon));
        setActivityIntent(i);
        getActivity();

        onView(withText(R.string.action_edit)).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.action_done)).check(matches(isDisplayed()));
    }

    public void testAlertsFragmentContent() throws Exception {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();

        Diagnostics diagnostics = new Diagnostics();
        diagnostics.alerts = new Diagnostics.Alert[0];
        diagnostics.beaconName = "";
        diagnostics.estimatedLowBatteryDate = null;

        when(mProximityApiService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));
        Intent i = new Intent(AttachmentsActivity.getStartIntent(getInstrumentation().getContext(), beacon));
        setActivityIntent(i);
        getActivity();

        onView(withText(R.string.fragment_title_alerts)).perform(click());
        onView(withId(R.id.text_battery_title)).check(matches(isDisplayed()));
        //TODO: Finish writing test when fragment implemented fully
    }

    public void testViewBeaconCompleteForm() throws Exception {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();

        Diagnostics diagnostics = new Diagnostics();
        diagnostics.alerts = new Diagnostics.Alert[0];
        diagnostics.beaconName = "";
        diagnostics.estimatedLowBatteryDate = null;

        when(mProximityApiService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));
        Intent i = new Intent(AttachmentsActivity.getStartIntent(getInstrumentation().getContext(), beacon));
        setActivityIntent(i);
        getActivity();

        onView(withId(R.id.text_title_beacon_name))
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_beacon_name))
                .check(matches(isDisplayed()))
                .check(matches(not(isFocusable())));

        onView(withId(R.id.text_title_advertised_id))
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_advertised_id))
                .check(matches(isDisplayed()))
                .check(matches(not(isFocusable())));
        onView(withId(R.id.text_advertised_id_error_message))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_description))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_description))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .check(matches(not(isFocusable())));

        onView(withId(R.id.text_title_type))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.spinner_type)).check(matches(not(isEnabled())));

        onView(withId(R.id.text_title_status))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.spinner_status)).check(matches(not(isEnabled())));
        onView(withId(R.id.text_status_error_message))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_stability))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.spinner_stability)).check(matches(not(isEnabled())));

        onView(withId(R.id.text_title_location))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_latitude))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .check(matches(not(isFocusable())));
        onView(withId(R.id.edit_text_longitude))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .check(matches(not(isFocusable())));
        onView(withId(R.id.text_title_place_id))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_place_id))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .check(matches(not(isFocusable())));
    }

    public void testViewBeaconIncompleteForm() throws Exception {
        Beacon beacon = MockModelsUtil.createMockIncompleteBeacon();

        Diagnostics diagnostics = new Diagnostics();
        diagnostics.alerts = new Diagnostics.Alert[0];
        diagnostics.beaconName = "";
        diagnostics.estimatedLowBatteryDate = null;

        when(mProximityApiService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));
        Intent i = new Intent(AttachmentsActivity.getStartIntent(getInstrumentation().getContext(), beacon));
        setActivityIntent(i);
        getActivity();

        onView(withId(R.id.text_title_beacon_name))
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_beacon_name))
                .check(matches(isDisplayed()))
                .check(matches(not(isFocusable())));

        onView(withId(R.id.text_title_advertised_id))
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_advertised_id))
                .check(matches(isDisplayed()))
                .check(matches(not(isFocusable())));
        onView(withId(R.id.text_advertised_id_error_message))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_description))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_text_description))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_type))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.spinner_type)).check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_status))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.spinner_status)).check(matches(not(isEnabled())));
        onView(withId(R.id.text_status_error_message))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_stability))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.spinner_stability)).check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_location))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_text_latitude))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_text_longitude))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.text_title_place_id))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_text_place_id))
                .check(matches(not(isDisplayed())));
    }

}