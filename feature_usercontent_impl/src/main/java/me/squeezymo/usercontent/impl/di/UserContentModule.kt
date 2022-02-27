package me.squeezymo.usercontent.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import me.squeezymo.core.ui.vmdelegate.IErrorHandlingVmDelegate
import me.squeezymo.usercontent.api.domain.usecase.*
import me.squeezymo.usercontent.impl.data.UserContentError
import me.squeezymo.usercontent.impl.domain.usecase.*
import me.squeezymo.usercontent.impl.ui.vmdelegate.UserContentErrorHandlingVmDelegate

@Module
@InstallIn(ViewModelComponent::class)
internal interface UserContentModule {

    @Binds
    @ViewModelScoped
    fun getTracksUC(
        impl: GetTracksUC
    ): IGetTracksUC

    @Binds
    @ViewModelScoped
    fun getPlaylistsUC(
        impl: GetPlaylistsUC
    ): IGetPlaylistsUC

    @Binds
    @ViewModelScoped
    fun findTracksUC(
        impl: FindTracksUC
    ): IFindTracksUC

    @Binds
    @ViewModelScoped
    fun findTrackIdUC(
        impl: FindTrackIdUC
    ): IFindTrackIdUC

    @Binds
    @ViewModelScoped
    fun addTrackExternalIdUC(
        impl: AddTrackExternalIdUC
    ): IAddTrackExternalIdUC

    @Binds
    @ViewModelScoped
    fun addTracksToLibraryUC(
        impl: AddTracksToLibraryUC
    ): IAddTracksToLibraryUC

    @Binds
    @ViewModelScoped
    fun addTracksToPlaylistUC(
        impl: AddTracksToPlaylistUC
    ): IAddTracksToPlaylistUC

    @Binds
    @ViewModelScoped
    fun createPlaylistUC(
        impl: CreatePlaylistUC
    ): ICreatePlaylistUC

    @Binds
    @ViewModelScoped
    fun userContentErrorHandlingVmDelegate(
        impl: UserContentErrorHandlingVmDelegate
    ): IErrorHandlingVmDelegate<UserContentError>

}
