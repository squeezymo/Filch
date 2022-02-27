package me.squeezymo.musicmigrator.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.filch.musicmigrator.databinding.AMainBinding
import dagger.hilt.android.AndroidEntryPoint
import me.squeezymo.analytics.api.DI_ANALYTICS_DELEGATE_ACTIVITY
import me.squeezymo.analytics.api.ui.delegate.IAnalyticsViewDelegate
import me.squeezymo.core.ui.BaseActivity
import me.squeezymo.core.ui.contract.SupportsCustomTabs
import me.squeezymo.core.ui.viewdelegate.addCustomTabsDelegate
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
internal class MainActivity :
    BaseActivity<AMainBinding, IMainViewModel>(),
    SupportsCustomTabs {

    override val viewModel: IMainViewModel by viewModels<MainViewModel>()

    private val customTabsDelegate = addCustomTabsDelegate()

    @Inject
    @Named(DI_ANALYTICS_DELEGATE_ACTIVITY)
    internal lateinit var analyticsDelegate: IAnalyticsViewDelegate

    override fun createViewBinding(
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    ): AMainBinding {
        return AMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            analyticsDelegate.appendScreenEvent("Запуск")
        }
    }

    override suspend fun requestNewCustomTabsSession(): CustomTabsSession? {
        return customTabsDelegate.requestNewSession()
    }

}
