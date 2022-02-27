package me.squeezymo.core.ui.contract

import androidx.browser.customtabs.CustomTabsSession

interface SupportsCustomTabs {

    suspend fun requestNewCustomTabsSession(): CustomTabsSession?

}
