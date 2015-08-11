package com.hitherejoe.watchtower;


import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.remote.WatchTowerService;
import com.hitherejoe.watchtower.injection.TestComponentRule;
import com.hitherejoe.watchtower.ui.activity.AddAttachmentActivity;
import com.hitherejoe.watchtower.util.MockModelsUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Observable;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class AddAttachmentActivityTest {

    @Rule
    public final ActivityTestRule<AddAttachmentActivity> main =
            new ActivityTestRule<>(AddAttachmentActivity.class, false, false);

    @Rule
    public final TestComponentRule component = new TestComponentRule();

    @Test
    public void testActivityDisplayed() {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        WatchTowerService.NamespacesResponse namespacesResponse = new WatchTowerService.NamespacesResponse();
        namespacesResponse.namespaces = MockModelsUtil.createMockListOfNamespaces(1);
        when(component.getMockWatchTowerService().getNamespaces())
                .thenReturn(Observable.just(namespacesResponse));

        Intent i = new Intent(AddAttachmentActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), beacon));
        main.launchActivity(i);

        onView(withId(R.id.text_namespace))
                .check(matches(isDisplayed()));
        onView(withId(R.id.spinner_namespace))
                .check(matches(isDisplayed()));
        onView(withId(R.id.text_data))
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_data))
                .check(matches(isDisplayed()));
        onView(withId(R.id.text_data_error_message))
                .check(matches(not(isDisplayed())));

        onData(allOf(is(instanceOf(String.class)), is(namespacesResponse.namespaces.get(0).namespaceName)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testAddAttachmentInvalidInput() {
        Beacon beacon = MockModelsUtil.createMockRegisteredBeacon();
        WatchTowerService.NamespacesResponse namespacesResponse = new WatchTowerService.NamespacesResponse();
        namespacesResponse.namespaces = MockModelsUtil.createMockListOfNamespaces(1);
        when(component.getMockWatchTowerService().getNamespaces())
                .thenReturn(Observable.just(namespacesResponse));

        Intent i = new Intent(AddAttachmentActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), beacon));
        main.launchActivity(i);

        onView(withId(R.id.text_data_error_message))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.action_done))
                .perform(click());
        onView(withText(R.string.dialog_error_blank_data))
                .check(matches(isDisplayed()));
        onView(withText(R.string.dialog_action_ok))
                .perform(click());
        onView(withId(R.id.text_data_error_message))
                .check(matches(isDisplayed()));
    }

    // Test for adding an attachment are found in AttachmentsActivityTest as the activity is finished, and we
    // need to test this

}