package com.adnd.capstonebackupfinal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.adnd.capstonebackupfinal.ui.MainActivity;
import com.adnd.capstonebackupfinal.ui.SettingsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by haymon on 2018-08-27.
 */

/**
 * Instrumented test, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    private static final String DETAIL_HEADER_TEXT = InstrumentationRegistry.getTargetContext().getResources().getString(R.string.label_name);
    private static final String SETTINGS_HEADER_TEXT = InstrumentationRegistry.getTargetContext().getResources().getString(R.string.settings_backup_by_label);

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<MainActivity>(MainActivity.class);

    @Rule
    public ActivityTestRule<SettingsActivity> settingsActivityRule =
            new ActivityTestRule<SettingsActivity>(SettingsActivity.class);

    @Test
    public void testMainViewWithScreenRotation() {
        //-test rotation on Main Activity
        rotateScreen(InstrumentationRegistry.getTargetContext(), mainActivityRule.getActivity());

        //-test 'Create'
        onView(withId(R.id.fab_btn)).perform(click());
        onView(withText(DETAIL_HEADER_TEXT)).check(matches(isDisplayed()));
        Espresso.pressBack();

        //test 'Settings'
        settingsActivityRule.launchActivity(getMockIntentData());
        rotateScreen(InstrumentationRegistry.getTargetContext(), settingsActivityRule.getActivity());
        onView(withText(SETTINGS_HEADER_TEXT)).check(matches(isDisplayed()));
        Espresso.pressBack();
    }

    /**
     * Mock Intent data for Settings
     */
    private Intent getMockIntentData() {
        Intent intent = new Intent(InstrumentationRegistry.getTargetContext(), SettingsActivity.class);
        return intent;
    }

    /**
     * Change the screen orientation (rotation)
     */
    public static void rotateScreen(Context context, Activity activity) {
        int orientation = context.getResources().getConfiguration().orientation;

        activity.setRequestedOrientation(
                (orientation == Configuration.ORIENTATION_PORTRAIT) ?
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
