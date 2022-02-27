package me.squeezymo.usersupport.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import me.squeezymo.usersupport.api.domain.usecase.IMarkAllChatMessagesAsReadChatUC
import me.squeezymo.usersupport.api.domain.usecase.IReceiveMessagesFromChatUC
import me.squeezymo.usersupport.api.domain.usecase.ISendMessageToChatUC
import me.squeezymo.usersupport.impl.domain.usecase.MarkAllChatMessagesAsReadUC
import me.squeezymo.usersupport.impl.domain.usecase.ReceiveMessagesFromChatUC
import me.squeezymo.usersupport.impl.domain.usecase.SendMessageToChatUC

@Module
@InstallIn(ViewModelComponent::class)
internal interface VmModule {

    @Binds
    @ViewModelScoped
    fun getReceiveMessagesFromChatUC(
        impl: ReceiveMessagesFromChatUC
    ): IReceiveMessagesFromChatUC

    @Binds
    @ViewModelScoped
    fun getSendMessageToChatUC(
        impl: SendMessageToChatUC
    ): ISendMessageToChatUC

    @Binds
    @ViewModelScoped
    fun getMarkAllChatMessagesAsReadUC(
        impl: MarkAllChatMessagesAsReadUC
    ): IMarkAllChatMessagesAsReadChatUC

}
