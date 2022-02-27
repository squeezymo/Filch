package me.squeezymo.core.uicomponents.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import me.squeezymo.core.ext.getDrawableOrThrow
import me.squeezymo.core.uicomponents.R
import me.squeezymo.core.uicomponents.databinding.VStreamingServiceBinding

abstract class BaseStreamingServiceWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    protected val binding = VStreamingServiceBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    init {
        orientation = VERTICAL
        showDividers = SHOW_DIVIDER_MIDDLE
        dividerDrawable = context.getDrawableOrThrow(R.drawable.space_h_6)
        gravity = Gravity.CENTER_HORIZONTAL

        binding.serviceLogoIv.clipToOutline = true
    }

    protected fun setServiceInfo(
        serviceUiName: String,
        isServiceEnabled: Boolean,
        @DrawableRes serviceIconResId: Int,
        @ColorInt serviceIconColor: Int,
        @DrawableRes backgroundResId: Int
    ) {
        binding.captionTv.text = serviceUiName
        binding.serviceLogoIv.setImageResource(serviceIconResId)

        if (isServiceEnabled) {
            binding.serviceLogoIv.isVisible = true
            binding.comingSoonTv.isGone = true
        }
        else {
            binding.serviceLogoIv.isGone = true
            binding.comingSoonTv.isVisible = true
        }

        binding.frame.setBackgroundResource(backgroundResId)
        binding.serviceLogoIv.setColorFilter(serviceIconColor)
    }

    fun showCaption(show: Boolean) {
        binding.captionTv.isInvisible = !show
    }

}
