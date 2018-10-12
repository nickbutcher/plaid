/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.ui

import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import android.view.Gravity
import io.plaidapp.R
import org.junit.Rule
import org.junit.Test

class HomeActivityTest {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(HomeActivity::class.java)

    @Test
    fun drawerClosedOnStartup() {
        // Given that the app is not launched

        // When the app is first launched

        // Then the drawer should be closed
        onView(withId(R.id.drawer)).check(matches(isClosed(Gravity.END)))
    }

    @Test
    fun pressFilterButtonOpensDrawer() {
        // Given that the drawer is closed
        onView(withId(R.id.drawer)).check(matches(isClosed(Gravity.END)))

        // When the filter button is pressed
        onView(withId(R.id.menu_filter)).perform(click())

        // Then the drawer should be opened
        onView(withId(R.id.drawer)).check(matches(isOpen(Gravity.END)))
    }

    @Test
    fun drawerStaysOpenAfterRotation() {
        // Given that the drawer is open
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withId(R.id.drawer)).check(matches(isOpen(Gravity.END)))

        // When rotating the device
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).setOrientationLeft()

        // Then the drawer should stay open after rotation
        onView(withId(R.id.drawer)).check(matches(isOpen(Gravity.END)))
    }
}
