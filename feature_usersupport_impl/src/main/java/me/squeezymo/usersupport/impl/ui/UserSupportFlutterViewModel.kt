package me.squeezymo.usersupport.impl.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import me.squeezymo.core.ui.BaseViewModel
import me.squeezymo.core.ui.IBaseViewModel
import javax.inject.Inject

internal interface IUserSupportFlutterViewModel : IBaseViewModel

@HiltViewModel
internal class UserSupportFlutterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel(savedStateHandle), IUserSupportFlutterViewModel
