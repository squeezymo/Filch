package me.squeezymo.analytics.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import me.squeezymo.analytics.api.DI_ANALYTICS_DELEGATE_ACTIVITY
import me.squeezymo.analytics.api.ui.delegate.IAnalyticsViewDelegate
import me.squeezymo.analytics.impl.ui.delegate.AnalyticsViewDelegate
import javax.inject.Named

@Module
@InstallIn(ActivityComponent::class)
internal interface ActivityModule {

    @Binds
    @Named(DI_ANALYTICS_DELEGATE_ACTIVITY)
    @ActivityScoped
    fun getAnalyticsViewDelegate(
        impl: AnalyticsViewDelegate
    ): IAnalyticsViewDelegate

}
