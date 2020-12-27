package io.plaidapp.ui


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import io.plaidapp.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CheckingToolbarItems {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(HomeActivity::class.java)

    @Test
    fun checkingToolbarItems() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val viewGroup = onView(
                allOf(withId(R.id.toolbar),
                        withParent(withParent(withId(R.id.drawer))),
                        isDisplayed()))
        viewGroup.check(matches(isDisplayed()))

        val textView = onView(
                allOf(withText("Plaid"),
                        withParent(allOf(withId(R.id.toolbar),
                                withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout::class.java)))),
                        isDisplayed()))
        textView.check(matches(withText("Plaid")))

        val textView2 = onView(
                allOf(withId(R.id.menu_search), withContentDescription("Search"),
                        withParent(withParent(withId(R.id.toolbar))),
                        isDisplayed()))
        textView2.check(matches(isDisplayed()))

        val checkBox = onView(
                allOf(withId(R.id.menu_theme),
                        withParent(withParent(withId(R.id.toolbar))),
                        isDisplayed()))
        checkBox.check(matches(isDisplayed()))

        val textView3 = onView(
                allOf(withId(R.id.menu_filter), withContentDescription("Filter"),
                        withParent(withParent(withId(R.id.toolbar))),
                        isDisplayed()))
        textView3.check(matches(isDisplayed()))

        val imageView = onView(
                allOf(withContentDescription("More options"),
                        withParent(withParent(withId(R.id.toolbar))),
                        isDisplayed()))
        imageView.check(matches(isDisplayed()))
    }
}
