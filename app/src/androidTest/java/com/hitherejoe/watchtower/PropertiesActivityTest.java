package com.hitherejoe.watchtower;


import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.ui.activity.PropertiesActivity;
import com.hitherejoe.watchtower.ui.fragment.PropertiesFragment;
import com.hitherejoe.watchtower.util.DataUtils;
import com.hitherejoe.watchtower.util.MockModelsUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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

@RunWith(AndroidJUnit4.class)
public class PropertiesActivityTest {

    @Rule
    public final ActivityTestRule<PropertiesActivity> main =
            new ActivityTestRule<>(PropertiesActivity.class, false, false);

    @Test
    public void testRegisterBeaconForm() {
        Intent i = new Intent(PropertiesActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), PropertiesFragment.Mode.REGISTER));
        main.launchActivity(i);

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
        String[] beaconTypes =
                InstrumentationRegistry.getTargetContext().getResources().getStringArray(R.array.types);
        for (String beaconType : beaconTypes) {
            onData(allOf(is(instanceOf(String.class)), is(beaconType))).check(matches(isDisplayed()));
            if (beaconTypes[beaconTypes.length - 1].equals(beaconType)) {
                onData(allOf(is(instanceOf(String.class)), is(beaconType))).perform(click());
            }
        }

        onView(withId(R.id.text_title_status))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.spinner_status)).perform(scrollTo(), click());

        String[] beaconStatuses =
                InstrumentationRegistry.getTargetContext().getResources().getStringArray(R.array.statuses);
        for (String beaconStatus : beaconStatuses) {
            onData(allOf(is(instanceOf(String.class)), is(beaconStatus))).check(matches(isDisplayed()));
            if (beaconStatuses[beaconStatuses.length - 1].equals(beaconStatus)) {
                onData(allOf(is(instanceOf(String.class)), is(beaconStatus))).perform(click());
            }
        }
        onView(withId(R.id.text_status_error_message))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.text_title_stability))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.spinner_stability)).perform(scrollTo(), click());

        String[] beaconStabilities =
                InstrumentationRegistry.getTargetContext().getResources().getStringArray(R.array.stabilities);
        for (String beaconStability : beaconStabilities) {
            onData(allOf(is(instanceOf(String.class)), is(beaconStability))).check(matches(isDisplayed()));
            if (beaconStabilities[beaconStabilities.length - 1].equals(beaconStability)) {
                onData(allOf(is(instanceOf(String.class)), is(beaconStability))).perform(click());
            }
        }
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


    public void testRegisterBeaconInvalidData() {
        Intent i = new Intent(PropertiesActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), PropertiesFragment.Mode.REGISTER));
        main.launchActivity(i);

        onView(withId(R.id.text_advertised_id_error_message))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.text_status_error_message))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_text_latitude))
                .perform(scrollTo(), typeText("f"));
        onView(withId(R.id.edit_text_longitude))
                .perform(scrollTo(), typeText("f"));
        onView(withId(R.id.action_done)).perform(click());
        onView(withId(R.id.text_advertised_id_error_message))
                .check(matches(isDisplayed()));
        onView(withId(R.id.text_status_error_message))
                .check(matches(isDisplayed()));
        onView(withId(R.id.text_latitude_error_message))
                .check(matches(isDisplayed()));
        onView(withId(R.id.text_longitude_error_message))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testUpdateBeacon() {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        Intent i = new Intent(PropertiesActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), beacon, PropertiesFragment.Mode.UPDATE));
        main.launchActivity(i);

        onView(withId(R.id.text_title_beacon_name))
                .check(matches(isDisplayed()));
        onView(withText(beacon.beaconName))
                .check(matches(isDisplayed()))
                .check(matches(not(isFocusable())));

        onView(withId(R.id.text_title_advertised_id))
                .check(matches(isDisplayed()));
        onView(withText(DataUtils.base64DecodeToString(beacon.advertisedId.id)))
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
        onView(withId(R.id.text_latitude_error_message))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.text_longitude_error_message))
                .check(matches(not(isDisplayed())));
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