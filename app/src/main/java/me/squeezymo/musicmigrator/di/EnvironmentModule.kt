package me.squeezymo.musicmigrator.di

import com.filch.musicmigrator.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.core.meta.FilchEnvironment
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface EnvironmentModule {

    companion object {

        @Provides
        @Singleton
        fun filchEnvironment(): FilchEnvironment {
            return FilchEnvironment(
                platform = "android",
                appVersion = BuildConfig.VERSION_NAME
            )
        }

    }

}
