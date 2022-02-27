package me.squeezymo.analytics.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import me.squeezymo.analytics.api.DI_ANALYTICS_DELEGATE_FRAGMENT
import me.squeezymo.analytics.api.ui.delegate.IAnalyticsViewDelegate
import me.squeezymo.analytics.impl.ui.delegate.AnalyticsViewDelegate
import javax.inject.Named

@Module
@InstallIn(FragmentComponent::class)
internal interface FragmentModule {

    @Binds
    @Named(DI_ANALYTICS_DELEGATE_FRAGMENT)
    @FragmentScoped
    fun getAnalyticsViewDelegate(
        impl: AnalyticsViewDelegate
    ): IAnalyticsViewDelegate

}
