package me.squeezymo.core.ui.viewdelegate

import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

fun ComponentActivity.addCustomTabsDelegate(): ICustomTabsActivityDelegate {
    return CustomTabsActivityDelegate().also {
        it.startObserving(this)
    }
}

interface ICustomTabsActivityDelegate {

    suspend fun requestNewSession(): CustomTabsSession?

}

internal class CustomTabsActivityDelegate(

) : ICustomTabsActivityDelegate {

    private val serviceConnection = FilchCustomTabsServiceConnection()
    private val client = MutableStateFlow<CustomTabsClient?>(null)

    fun startObserving(activity: ComponentActivity) {
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    CustomTabsClient.bindCustomTabsService(
                        activity,
                        "com.android.chrome",
                        serviceConnection
                    )
                }
            }
        )
    }

    override suspend fun requestNewSession(): CustomTabsSession? {
        return waitForClient().newSession(null)
    }

    private suspend fun waitForClient(): CustomTabsClient {
        return client
            .filterNotNull()
            .first()
    }

    private inner class FilchCustomTabsServiceConnection : CustomTabsServiceConnection() {

        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            this@CustomTabsActivityDelegate.client.value = client
            client.warmup(0)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            this@CustomTabsActivityDelegate.client.value = null
        }

    }

}
