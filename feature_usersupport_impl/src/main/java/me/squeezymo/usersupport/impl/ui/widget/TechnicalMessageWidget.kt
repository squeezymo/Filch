package me.squeezymo.usersupport.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import me.squeezymo.core.ui.recyclerview.BaseAdapterDelegate
import me.squeezymo.usersupport.impl.R
import me.squeezymo.usersupport.impl.databinding.VTechnicalMessageBinding
import me.squeezymo.usersupport.impl.ui.viewobject.ChatMessageUi

internal class TechnicalMessageWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = VTechnicalMessageBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    fun setState(state: ChatMessageUi.Technical) {
        binding.bodyTv.text = when (state) {
            ChatMessageUi.Technical.Predefined.Header -> {
                resources.getString(R.string.user_support_technical_message_header)
            }
            is ChatMessageUi.Technical.Arbitrary -> {
                state.body
            }
        }
    }

    companion object {

        private fun create(
            context: Context
        ): TechnicalMessageWidget {
            return TechnicalMessageWidget(context)
        }

        fun createAdapterDelegate() = BaseAdapterDelegate(
            ChatMessageUi.Technical::class,
            { context ->
                create(context)
            },
            updateState = { widget, state, _ ->
                widget.setState(state)
            }
        )

    }

}
