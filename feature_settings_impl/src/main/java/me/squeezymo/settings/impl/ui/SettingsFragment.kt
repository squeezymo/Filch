package me.squeezymo.settings.impl.ui

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.squeezymo.analytics.api.DI_ANALYTICS_DELEGATE_ACTIVITY
import me.squeezymo.analytics.api.DI_ANALYTICS_DELEGATE_FRAGMENT
import me.squeezymo.analytics.api.ui.delegate.IAnalyticsViewDelegate
import me.squeezymo.core.ui.BaseChildFragment
import me.squeezymo.core.ui.util.TextHighlighter
import me.squeezymo.settings.impl.R
import me.squeezymo.settings.impl.databinding.FSettingsBinding
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
internal class SettingsFragment :
    BaseChildFragment<FSettingsBinding, ISettingsViewModel, ISettingsRootViewModel>() {

    override val viewModel: ISettingsViewModel
            by viewModels<SettingsViewModel>()

    override val rootViewModel: ISettingsRootViewModel
            by navGraphViewModels<SettingsRootViewModel>(R.id.nav_settings_graph)

    @Inject
    @Named(DI_ANALYTICS_DELEGATE_FRAGMENT)
    internal lateinit var analyticsDelegate: IAnalyticsViewDelegate

    private var customTabsSession: CustomTabsSession? = null

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FSettingsBinding {
        return FSettingsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analyticsDelegate.bind(viewLifecycleOwner) { "О приложении" }

        binding.aboutTv.text = TextHighlighter(Regex("<b>(.*?)</b>"))
            .createHighlightableText(
                getString(R.string.settings_text_about),
                highlightSpan = {
                    StyleSpan(Typeface.BOLD)
                }
            )
        binding.shareBtn.setOnClickListener {
            // TODO
            Toast.makeText(
                requireContext(),
                "TODO: Добавить шаринг перед публикацией",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.goToPrivacyPolicyTv.setOnClickListener {
            createCustomTabsIntentBuilder(requireContext())
                .build()
                .launchUrl(requireContext(), Uri.parse(getString(R.string.url_privacy_policy)))
        }
        binding.goToClientSupportTv.setOnClickListener {
            viewModel.goToUserSupport()
        }
        binding.logoutBtn.setOnClickListener {
            viewModel.goToLogoutPicker()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    requestNewCustomTabsSession()?.let { newCustomTabsSession ->
                        newCustomTabsSession.mayLaunchUrl(
                            Uri.parse(getString(R.string.url_privacy_policy)),
                            null,
                            null
                        )
                        customTabsSession = newCustomTabsSession
                    }
                }
            }
        }
    }

    private fun createCustomTabsIntentBuilder(
        context: Context
    ): CustomTabsIntent.Builder {
        return CustomTabsIntent.Builder()
            .setShowTitle(true)
            // Chrome enter, App exit
            .setStartAnimations(context, R.anim.slide_in_bottom, 0)
            //.setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
            // App enter, Chrome exit
            .setExitAnimations(context, 0, R.anim.slide_out_bottom)
            //.setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
            .setUrlBarHidingEnabled(true)
            .let { builder ->
                val session = customTabsSession
                if (session == null) builder else builder.setSession(session)
            }
    }

}
