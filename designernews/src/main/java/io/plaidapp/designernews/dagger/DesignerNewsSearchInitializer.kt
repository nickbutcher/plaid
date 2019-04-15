package io.plaidapp.designernews.dagger

import android.content.Context
import io.plaidapp.core.interfaces.ModuleInitializer

object DesignerNewsSearchInitializer : ModuleInitializer {

    override fun init(context: Context) {
        DaggerDesignerNewsSearchComponent.builder()
            .designerNewsPreferencesModule(
                DesignerNewsPreferencesModule(context)
            )
            .build()
    }
}
