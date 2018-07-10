package io.plaidapp.about.ui.model

import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Resources
import android.text.SpannableString
import androidx.core.text.getSpans
import io.plaidapp.about.ui.AboutStyler
import io.plaidapp.core.util.LiveDataTestUtil
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito

class AboutViewModelTest {


    private val activity = Mockito.mock(Activity::class.java)
    private val resources = Mockito.mock(Resources::class.java)

    private val aboutViewModel = AboutViewModel(AboutStyler(activity), resources)

    init {
        Mockito.`when`(activity.resources).thenReturn(resources)
        Mockito.`when`(resources.getColorStateList(anyInt(), any(Resources.Theme::class.java)))
            .thenReturn(
                ColorStateList.valueOf(0xff00ff)
            )
        Mockito.`when`(resources.getColor(anyInt(), any(Resources.Theme::class.java)))
            .thenReturn(
                0x00ff00
            )
    }

    @Test
    fun onLibraryClick() {
        // Click on all the libraries
        aboutViewModel.libraries.forEach {
            aboutViewModel.onLibraryClick(it)
            val event = LiveDataTestUtil.getValue(aboutViewModel.navigationTarget)
            assertThat(event?.peek(), `is`(equalTo(it.link)))
        }
    }

    @Test
    fun spansCreated_appAboutText() {
        testSpannable(aboutViewModel.appAboutText)
    }

    @Test
    fun spansCreated_iconAboutText() {
        testSpannable(aboutViewModel.iconAboutText)
    }

    private fun testSpannable(text: CharSequence) {
        val spannable = SpannableString(text)
        assertNotEquals(0, spannable.getSpans<Any>().size)
    }
}
