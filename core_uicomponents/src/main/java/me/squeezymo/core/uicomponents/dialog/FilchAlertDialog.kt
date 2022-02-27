package me.squeezymo.core.uicomponents.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updateMarginsRelative
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import me.squeezymo.core.ext.clipWithRoundRect
import me.squeezymo.core.ext.dp
import me.squeezymo.core.ext.getDrawableOrThrow
import me.squeezymo.core.uicomponents.R

class FilchAlertDialogBuilder(
    private val context: Context,
    private val text: String?,
    private val buttons: List<Button>
) {

    class Button(
        val text: String,
        val isBold: Boolean = false,
        val isItalic: Boolean = false,
        val clickListener: () -> Unit
    )

    fun build(): AlertDialog.Builder {
        return MaterialAlertDialogBuilder(context)
            .setBackground(ColorDrawable(Color.TRANSPARENT))
            .setView(createDialogView())
            .setCancelable(true)
    }

    private fun createDialogView(): View {
        return FilchAlertDialogView(context).also { view ->
            view.setState(text, buttons)
        }
    }

}

private class FilchAlertDialogView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        setBackgroundResource(R.color.bg_secondary)
        orientation = VERTICAL
        showDividers = SHOW_DIVIDER_MIDDLE
        dividerDrawable = context.getDrawableOrThrow(R.drawable.divider_dialog)
        gravity = Gravity.CENTER_HORIZONTAL
        clipWithRoundRect(dp(8))
    }

    fun setState(
        text: String?,
        buttons: List<FilchAlertDialogBuilder.Button>
    ) {
        removeAllViews()

        if (text != null) {
            addView(
                createText(text),
                MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).also { lparams ->
                    lparams.updateMarginsRelative(
                        start = dp(16),
                        top = dp(18),
                        end = dp(16),
                        bottom = dp(18)
                    )
                }
            )
        }

        if (buttons.isNotEmpty()) {
            buttons.forEach { button ->
                addView(
                    createButton(button),
                    MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }
    }

    private fun createText(text: String): View {
        return MaterialTextView(context).also { tv ->
            tv.text = text
            tv.textSize = 18F
            tv.setTypeface(tv.typeface, Typeface.BOLD)
            tv.gravity = Gravity.CENTER_HORIZONTAL
        }
    }

    @SuppressLint("InflateParams")
    private fun createButton(state: FilchAlertDialogBuilder.Button): View {
        // The only way to apply button style programmatically that works
        val btn = LayoutInflater
            .from(context)
            .inflate(R.layout.v_dialog_button, null) as MaterialButton

        btn.text = state.text
        btn.setTextColor(Color.parseColor("#0A84FF")) // TODO Use dedicated resource
        btn.setTypeface(
            btn.typeface,
            when {
                state.isBold && state.isItalic -> Typeface.BOLD_ITALIC
                state.isBold -> Typeface.BOLD
                state.isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
        )
        btn.setOnClickListener {
            state.clickListener()
        }

        return btn
    }

}
