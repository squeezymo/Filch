package me.squeezymo.settings.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.textview.MaterialTextView
import me.squeezymo.core.ext.dp
import me.squeezymo.core.ext.getColorCompat
import me.squeezymo.core.ext.getDrawableOrThrow
import me.squeezymo.core.ext.useAndRecycle
import me.squeezymo.settings.impl.R

internal class NumericStepWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val stepNumTv = createStepNumTv()
    private val stepTextTv = createTextTv()

    init {
        orientation = HORIZONTAL
        showDividers = SHOW_DIVIDER_MIDDLE
        dividerDrawable = context.getDrawableOrThrow(R.drawable.space_w_8)

        addView(
            stepNumTv,
            LayoutParams(
                dp(22),
                dp(22)
            )
        )

        addView(
            stepTextTv,
            LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also { lparams ->
                lparams.weight = 1F
            }
        )

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.NumericStepWidget,
            0,
            0
        ).useAndRecycle {
            val hasStepNumber = hasValue(R.styleable.NumericStepWidget_stepNumber)
            val stepNumber = getInt(R.styleable.NumericStepWidget_stepNumber, 0)
            val stepText = getString(R.styleable.NumericStepWidget_stepText) ?: ""

            if (isInEditMode && !hasStepNumber) {
                setStepNumber(2)
                setStepText("идёт процесс поиска доступных песен и создаётся список")
            }
            else {
                setStepNumber(stepNumber)
                setStepText(stepText)
            }
        }
    }

    fun setStepNumber(num: Int) {
        stepNumTv.text = num.toString()
    }

    fun setStepText(text: String) {
        stepTextTv.text = text
    }

    private fun createStepNumTv(): TextView {
        return MaterialTextView(context).apply {
            textSize = 16F
            gravity = Gravity.CENTER
            setTextColor(context.getColorCompat(R.color.white_a60))
            setBackgroundResource(R.drawable.bg_oval_s_white_a10)
        }
    }

    private fun createTextTv(): TextView {
        return MaterialTextView(context).apply {
            textSize = 16F
            setTextColor(context.getColorCompat(R.color.white_a60))
        }
    }

}
