package me.squeezymo.usersupport.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.usersupport.api.data.repository.IChatRepository
import me.squeezymo.usersupport.impl.data.repository.ChatRepository
import me.squeezymo.usersupport.impl.network.ChatDataSource
import me.squeezymo.usersupport.impl.network.IChatDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface SingletonModule {

    @Binds
    @Singleton
    fun getChatRepository(
        impl: ChatRepository
    ): IChatRepository

    @Binds
    fun getChatDataSource(
        impl: ChatDataSource
    ): IChatDataSource

}
