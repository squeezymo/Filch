package me.squeezymo.usersupport.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import me.squeezymo.core.ui.recyclerview.BaseAdapterDelegate
import me.squeezymo.usersupport.impl.databinding.VIncomingMessageBinding
import me.squeezymo.usersupport.impl.ui.viewobject.ChatMessageUi

internal class IncomingMessageWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = VIncomingMessageBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    fun setState(state: ChatMessageUi.Incoming) {
        binding.bodyTv.text = state.body
    }

    companion object {

        private fun create(
            context: Context
        ): IncomingMessageWidget {
            return IncomingMessageWidget(context)
        }

        fun createAdapterDelegate() = BaseAdapterDelegate(
            ChatMessageUi.Incoming::class,
            { context ->
                create(context)
            },
            updateState = { widget, state, _ ->
                widget.setState(state)
            }
        )

    }

}
