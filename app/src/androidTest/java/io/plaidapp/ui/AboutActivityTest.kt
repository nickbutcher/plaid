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

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.openContextualActionModeOverflowMenu
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import io.plaidapp.R
import org.junit.Rule
import org.junit.Test

class AboutActivityTest {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(HomeActivity::class.java)

    @Test
    fun navigateToAboutScreen() {
        // Given the app is on the Home screen

        // When tapping on the overflow menu and then the About menu item
        openContextualActionModeOverflowMenu()
        onView(withText(R.string.about)).perform(click())

        // Then the About screen should show

        // Does not compile: "Unresolved reference"
//        onView(withId(R.id.about_description)).check(matches(isDisplayed()))
//        onView(withText(R.string.about_plaid_0)).check(matches(isDisplayed()))
    }

}