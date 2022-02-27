package me.squeezymo.usersupport.impl.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.plugin.common.MethodChannel
import me.squeezymo.analytics.api.DI_ANALYTICS_DELEGATE_FRAGMENT
import me.squeezymo.analytics.api.ui.delegate.IAnalyticsViewDelegate
import me.squeezymo.core.ui.BaseFragment
import me.squeezymo.usersupport.api.FLUTTER_ENGINE_USER_SUPPORT
import me.squeezymo.usersupport.impl.FLUTTER_CHANNEL_FLUTTER_TO_NATIVE
import me.squeezymo.usersupport.impl.FLUTTER_CHANNEL_NATIVE_TO_FLUTTER
import me.squeezymo.usersupport.impl.R
import me.squeezymo.usersupport.impl.databinding.FFlutterUserSupportBinding
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
internal class UserSupportFlutterFragment :
    BaseFragment<FFlutterUserSupportBinding, IUserSupportFlutterViewModel>() {

    companion object {
        private const val TAG_FLUTTER_FRAGMENT = "flutter_fragment"
    }

    override val viewModel: IUserSupportFlutterViewModel by viewModels<UserSupportFlutterViewModel>()

    private var flutterFragment: FlutterFragment? = null

    @Inject
    internal lateinit var gsonBuilder: GsonBuilder

    @Inject
    @Named(DI_ANALYTICS_DELEGATE_FRAGMENT)
    internal lateinit var analyticsDelegate: IAnalyticsViewDelegate

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FFlutterUserSupportBinding {
        return FFlutterUserSupportBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        flutterFragment = childFragmentManager
            .findFragmentByTag(TAG_FLUTTER_FRAGMENT) as FlutterFragment?

        if (flutterFragment == null) {
            childFragmentManager
                .beginTransaction()
                .add(
                    R.id.flutter_container,
                    FlutterFragment
                        .withCachedEngine(FLUTTER_ENGINE_USER_SUPPORT)
                        .build<FlutterFragment>()
                        .also {
                            flutterFragment = it
                        },
                    TAG_FLUTTER_FRAGMENT
                )
                .runOnCommit {
                    setupChannels()
                }
                .commit()
        }
    }

    private fun setupChannels() {
        val flutterEngine = checkNotNull(
            FlutterEngineCache
                .getInstance()
                .get(FLUTTER_ENGINE_USER_SUPPORT)
        ) {
            "Expecting to find warmed-up flutter engine by id=$FLUTTER_ENGINE_USER_SUPPORT"
        }
        val messenger = flutterEngine.dartExecutor.binaryMessenger
        val channelNativeToFlutter = MethodChannel(
            messenger,
            FLUTTER_CHANNEL_NATIVE_TO_FLUTTER
        )
        val channelFlutterToNative = MethodChannel(
            messenger,
            FLUTTER_CHANNEL_FLUTTER_TO_NATIVE
        )
        val gson = gsonBuilder.create()

        channelFlutterToNative.setMethodCallHandler { call, _ ->
            if (call.method == "requestUserFlow") {
                sendUserFlow(channelNativeToFlutter, gson)
            }
        }

        channelNativeToFlutter.invokeMethod(
            "setData",
            gson.toJson(
                mapOf(
                    "appId" to "Filch",
                    "platform" to "android",
                    "appVersion" to "1.0",
                    "supportId" to "SUPPORT"
                )
            )
        )
    }

    private fun sendUserFlow(
        channel: MethodChannel,
        gson: Gson
    ) {
        channel.invokeMethod(
            "sendUserFlow",
            gson.toJson(
                analyticsDelegate
                    .getUserFlowEvents()
                    .mapIndexed { index, userFlowEvent ->
                        mapOf(
                            "step" to index + 1,
                            "message" to userFlowEvent.getMessage(),
                            "type" to userFlowEvent.getType()
                        )
                    }
            )
        )
    }

}
