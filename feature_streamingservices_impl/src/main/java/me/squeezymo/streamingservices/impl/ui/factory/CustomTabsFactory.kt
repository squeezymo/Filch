package me.squeezymo.streamingservices.impl.ui.factory

import android.content.Context
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import dagger.hilt.android.scopes.ActivityScoped
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.streamingservices.impl.R
import javax.inject.Inject

@ActivityScoped
internal class CustomTabsFactory @Inject constructor() {

    fun createCustomTabsIntentBuilder(
        context: Context,
        serviceId: StreamingServiceID
    ): CustomTabsIntent.Builder {
        return CustomTabsIntent.Builder()
            .setShowTitle(true)
            // Chrome enter, App exit
            .setStartAnimations(context, R.anim.slide_in_bottom, 0)
            //.setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
            // App enter, Chrome exit
            .setExitAnimations(context, 0, R.anim.slide_out_bottom)
            //.setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
            .setDefaultColorSchemeParams(createDefaultColorSchemeParamsBuilder(serviceId).build())
    }

    private fun createDefaultColorSchemeParamsBuilder(
        serviceId: StreamingServiceID
    ): CustomTabColorSchemeParams.Builder {
        val streamingService = StreamingService.requireById(serviceId)

        return CustomTabColorSchemeParams
            .Builder()
    }

}
