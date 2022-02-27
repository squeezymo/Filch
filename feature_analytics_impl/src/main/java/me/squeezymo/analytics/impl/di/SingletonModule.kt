package me.squeezymo.analytics.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.analytics.api.domain.controller.IAnalyticsController
import me.squeezymo.analytics.impl.domain.controller.AnalyticsController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface SingletonModule {

    @Binds
    @Singleton
    fun getAnalyticsController(
        impl: AnalyticsController
    ): IAnalyticsController

}
