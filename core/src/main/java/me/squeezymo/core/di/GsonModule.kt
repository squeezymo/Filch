package me.squeezymo.core.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface GsonModule {

    companion object {

        @Provides
        @Singleton
        fun gsonBuilder(): GsonBuilder {
            return GsonBuilder()
        }

    }

}
