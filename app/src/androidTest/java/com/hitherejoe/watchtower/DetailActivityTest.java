package com.hitherejoe.watchtower;


import android.content.Intent;

import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.model.Diagnostics;
import com.hitherejoe.watchtower.ui.activity.AttachmentsActivity;
import com.hitherejoe.watchtower.ui.activity.DetailActivity;
import com.hitherejoe.watchtower.util.MockModelsUtil;
import com.hitherejoe.watchtower.util.BaseTestCase;

import rx.Observable;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
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

        when(mWatchTowerService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));

        Intent i = new Intent(DetailActivity.getStartIntent(getInstrumentation().getContext(), beacon));
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

        when(mWatchTowerService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));

        Intent i = new Intent(DetailActivity.getStartIntent(getInstrumentation().getContext(), beacon));
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

        when(mWatchTowerService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));
        Intent i = new Intent(DetailActivity.getStartIntent(getInstrumentation().getContext(), beacon));
        setActivityIntent(i);
        getActivity();

        onView(withText(R.string.fragment_title_alerts)).perform(click());
        onView(withId(R.id.text_battery_title)).check(matches(isDisplayed()));
        onView(withId(R.id.text_title_alerts)).check(matches(isDisplayed()));
    }

    public void testDiagnosticsInformationDisplayed() {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        Diagnostics diagnostics = MockModelsUtil.createMockDiagnostics(beacon.beaconName);

        when(mWatchTowerService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));
        Intent i = new Intent(DetailActivity.getStartIntent(getInstrumentation().getContext(), beacon));
        setActivityIntent(i);
        getActivity();

        onView(withText(R.string.fragment_title_alerts)).perform(click());
        onView(withText(R.string.text_title_estimated_battery)).check(matches(isDisplayed()));
        onView(withText(diagnostics.estimatedLowBatteryDate.buildDate())).check(matches(isDisplayed()));
        for (Diagnostics.Alert alert : diagnostics.alerts) {
            onView(withText(alert.toString())).check(matches(isDisplayed()));
        }
    }

    public void testDiagnosticsPlaceholdersDisplayed() {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        Diagnostics diagnostics = MockModelsUtil.createMockEmptyDiagnostics(beacon.beaconName);

        when(mWatchTowerService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));
        Intent i = new Intent(DetailActivity.getStartIntent(getInstrumentation().getContext(), beacon));
        setActivityIntent(i);
        getActivity();

        onView(withText(R.string.fragment_title_alerts)).perform(click());
        onView(withText(R.string.text_title_estimated_battery)).check(matches(isDisplayed()));
        onView(withText(R.string.text_battery_unknown)).check(matches(isDisplayed()));
        onView(withText(R.string.text_no_alerts)).check(matches(isDisplayed()));
    }

    public void testViewBeaconCompleteForm() throws Exception {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();

        Diagnostics diagnostics = new Diagnostics();
        diagnostics.alerts = new Diagnostics.Alert[0];
        diagnostics.beaconName = "";
        diagnostics.estimatedLowBatteryDate = null;

        when(mWatchTowerService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));
        Intent i = new Intent(DetailActivity.getStartIntent(getInstrumentation().getContext(), beacon));
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

        when(mWatchTowerService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));
        Intent i = new Intent(DetailActivity.getStartIntent(getInstrumentation().getContext(), beacon));
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

    public void testUpdateBeacon() throws Exception {
        Beacon beacon = MockModelsUtil.createMockIncompleteBeacon();
        Beacon registeredBeacon = MockModelsUtil.createMockRegisteredBeacon();

        Diagnostics diagnostics = new Diagnostics();
        diagnostics.alerts = new Diagnostics.Alert[0];
        diagnostics.beaconName = "";
        diagnostics.estimatedLowBatteryDate = null;

        when(mWatchTowerService.beaconDiagnostics(beacon.beaconName))
                .thenReturn(Observable.just(diagnostics));
        when(mWatchTowerService.updateBeacon(anyString(), any(Beacon.class)))
                .thenReturn(Observable.just(registeredBeacon));


        Intent i = new Intent(DetailActivity.getStartIntent(getInstrumentation().getContext(), beacon));
        setActivityIntent(i);
        getActivity();

        onView(withId(R.id.action_edit)).perform(click());

        onView(withId(R.id.edit_text_description))
                .perform(scrollTo(), typeText("New description"));

        onView(withId(R.id.spinner_type)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("AltBeacon"))).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.text_title_status))
                .perform(scrollTo());
        onView(withId(R.id.spinner_status)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("Active"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Inactive"))).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.text_title_stability))
                .perform(scrollTo());
        onView(withId(R.id.spinner_stability)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("Roving"))).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.edit_text_latitude))
                .perform(scrollTo(), typeText("-4.92344"));
        onView(withId(R.id.edit_text_longitude))
                .perform(scrollTo(), typeText("2.93784"));
        onView(withId(R.id.edit_text_place_id))
                .perform(scrollTo(), typeText("hbsj83hDHDB84635"));

        onView(withId(R.id.action_done)).perform(click());
        onView(withText(R.string.fragment_title_properties)).check(matches(isDisplayed()));
        onView(withText(R.string.fragment_title_alerts)).check(matches(isDisplayed()));
    }

}