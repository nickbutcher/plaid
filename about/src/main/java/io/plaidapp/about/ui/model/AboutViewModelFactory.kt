package io.plaidapp.about.ui.model

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.res.Resources
import io.plaidapp.about.ui.AboutStyler

/**
 * Factory to create [AboutViewModel]
 */
internal class AboutViewModelFactory(
    val aboutStyler: AboutStyler,
    val resources: Resources
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AboutViewModel::class.java)) {
            AboutViewModel(aboutStyler, resources) as T
        } else {
            throw IllegalArgumentException(
                "Class ${modelClass.name} is not supported in this factory."
            )
        }
    }

}