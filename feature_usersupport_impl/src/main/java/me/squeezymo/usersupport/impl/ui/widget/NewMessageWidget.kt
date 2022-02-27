package me.squeezymo.usersupport.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.widget.LinearLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import me.squeezymo.core.ext.getColorCompat
import me.squeezymo.core.ext.getDrawableOrThrow
import me.squeezymo.usersupport.impl.R
import me.squeezymo.usersupport.impl.databinding.VNewMessageBinding

internal class NewMessageWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = VNewMessageBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    private var onSubmitListener: (String) -> Unit = { /* do nothing */ }
    private val onSubmitClickListener = OnClickListener {
        onSubmitListener(binding.inputEt.text.toString())
    }

    private var isSendingMessage: Boolean = false

    init {
        orientation = HORIZONTAL
        showDividers = SHOW_DIVIDER_MIDDLE
        dividerDrawable = context.getDrawableOrThrow(R.drawable.space_w_4)

        adjustSubmitBtnState(binding.inputEt.text, isSendingMessage)

        binding.inputEt.doOnTextChanged { text, _, _, _ ->
            adjustSubmitBtnState(text, isSendingMessage)
        }
    }

    fun setOnSubmitListener(listener: (String) -> Unit) {
        this.onSubmitListener = listener
    }

    fun setIsSendingMessage(isSendingMessage: Boolean) {
        this.isSendingMessage = isSendingMessage

        if (isSendingMessage) {
            binding.submitIv.isInvisible = true
            binding.submissionInProgressPi.isVisible = true
        }
        else {
            binding.submitIv.isVisible = true
            binding.submissionInProgressPi.isInvisible = true
        }

        adjustSubmitBtnState(binding.inputEt.text, isSendingMessage)
    }

    fun clear() {
        binding.inputEt.setText("")
    }

    private fun adjustSubmitBtnState(text: CharSequence?, isSendingMessage: Boolean) {
        if (text.isNullOrBlank() || isSendingMessage) {
            binding.submitIv.setColorFilter(context.getColorCompat(R.color.white_a20))
            binding.submitContainer.setOnClickListener(null)
        }
        else {
            binding.submitIv.setColorFilter(context.getColorCompat(R.color.white))
            binding.submitContainer.setOnClickListener(onSubmitClickListener)
        }
    }

}
