package me.squeezymo.settings.impl.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import me.squeezymo.core.ui.BaseViewModel
import me.squeezymo.core.ui.IBaseViewModel
import me.squeezymo.settings.api.navigation.LogoutServicePickerDeepLink
import javax.inject.Inject

internal interface ISettingsViewModel : IBaseViewModel {

    fun goToLogoutPicker()

    fun goToUserSupport()

}

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel(savedStateHandle), ISettingsViewModel {

    override fun goToLogoutPicker() {
        navigateTo(
            LogoutServicePickerDeepLink.create()
        )
    }

    override fun goToUserSupport() {
        navigateTo(
            SettingsFragmentDirections.actionSettingsToUserSupport()
        )
    }

}
