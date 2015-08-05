package com.hitherejoe.watchtower;


import android.content.Intent;

import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.ui.activity.PropertiesActivity;
import com.hitherejoe.watchtower.ui.fragment.PropertiesFragment;
import com.hitherejoe.watchtower.util.MockModelsUtil;
import com.hitherejoe.watchtower.util.BaseTestCase;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isFocusable;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class PropertiesActivityTest extends BaseTestCase<PropertiesActivity> {

    public PropertiesActivityTest() {
        super(PropertiesActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testRegisterBeaconForm() throws Exception {
        Intent i = new Intent(PropertiesActivity.getStartIntent(getInstrumentation().getContext(), PropertiesFragment.Mode.REGISTER));
        setActivityIntent(i);
        getActivity();

        onView(withId(R.id.text_title_beacon_name))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_text_beacon_name))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_advertised_id))
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_advertised_id))
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_advertised_id))
                .check(matches(isFocusable()));
        onView(withId(R.id.text_advertised_id_error_message))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_description))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_description))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.text_title_type))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.spinner_type)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("Eddystone"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("iBeacon"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("AltBeacon"))).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.text_title_status))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.spinner_status)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("Active"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Inactive"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Decommissioned"))).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.text_status_error_message))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_stability))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.spinner_stability)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("Stable"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Portable"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Mobile"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Roving"))).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.text_title_location))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_latitude))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_longitude))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.text_title_place_id))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_place_id))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    public void testRegisterBeaconInvalidData() throws Exception {
        Intent i = new Intent(PropertiesActivity.getStartIntent(getInstrumentation().getContext(), PropertiesFragment.Mode.REGISTER));
        setActivityIntent(i);
        getActivity();

        onView(withId(R.id.text_advertised_id_error_message))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.text_status_error_message))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.action_done)).perform(click());
        onView(withId(R.id.text_advertised_id_error_message))
                .check(matches(isDisplayed()));
        onView(withId(R.id.text_status_error_message))
                .check(matches(isDisplayed()));
    }

    public void testUpdateBeacon() throws Exception {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        Intent i = new Intent(PropertiesActivity.getStartIntent(getInstrumentation().getContext(), beacon, PropertiesFragment.Mode.UPDATE));
        setActivityIntent(i);
        getActivity();

        onView(withId(R.id.text_title_beacon_name))
                .check(matches(isDisplayed()));
        onView(withText(beacon.beaconName))
                .check(matches(isDisplayed()))
                .check(matches(not(isFocusable())));

        onView(withId(R.id.text_title_advertised_id))
                .check(matches(isDisplayed()));
        onView(withText(beacon.advertisedId.id))
                .check(matches(isDisplayed()))
                .check(matches(not(isFocusable())));
        onView(withId(R.id.text_advertised_id_error_message))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_description))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withText(beacon.description))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.text_title_type))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withText(beacon.advertisedId.type.getString())).perform(scrollTo()).check(matches(isDisplayed()));

        onView(withId(R.id.text_title_status))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withText(beacon.status.getString())).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.text_status_error_message))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_stability))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withText(beacon.expectedStability.getString())).perform(scrollTo()).check(matches(isDisplayed()));

        onView(withId(R.id.text_title_location))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withText(String.valueOf(beacon.latLng.latitude)))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withText(String.valueOf(beacon.latLng.longitude)))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.text_title_place_id))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withText(beacon.placeId))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // Tests for registering are found in MainActivityTest and updating beacon are found in DetailActivityTest,
    // this is because the activity gets closed upon success, so we need to test this functions correctly

}