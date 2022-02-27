package me.squeezymo.settings.impl.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import me.squeezymo.core.ui.BaseViewModel
import me.squeezymo.core.ui.IBaseViewModel
import javax.inject.Inject

internal interface IUserSupportContainerViewModel : IBaseViewModel

@HiltViewModel
internal class UserSupportContainerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel(savedStateHandle), IUserSupportContainerViewModel
