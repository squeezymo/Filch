package me.squeezymo.usersupport.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import me.squeezymo.core.ui.recyclerview.BaseAdapterDelegate
import me.squeezymo.usersupport.impl.databinding.VOutgoingMessageBinding
import me.squeezymo.usersupport.impl.ui.viewobject.ChatMessageUi

internal class OutgoingMessageWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = VOutgoingMessageBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    fun setState(state: ChatMessageUi.Outgoing) {
        binding.bodyTv.text = state.body
    }

    companion object {

        private fun create(
            context: Context
        ): OutgoingMessageWidget {
            return OutgoingMessageWidget(context)
        }

        fun createAdapterDelegate() = BaseAdapterDelegate(
            ChatMessageUi.Outgoing::class,
            { context ->
                create(context)
            },
            updateState = { widget, state, _ ->
                widget.setState(state)
            }
        )

    }

}
