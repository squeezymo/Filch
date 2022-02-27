package me.squeezymo.usercontent.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import me.squeezymo.core.ext.getDrawableOrThrow
import me.squeezymo.core.ext.setTextOrMakeGone
import me.squeezymo.core.ui.recyclerview.BaseAdapterDelegate
import me.squeezymo.usercontent.impl.R
import me.squeezymo.usercontent.impl.databinding.VPlaylistLoadingBinding
import me.squeezymo.usercontent.impl.ui.viewobject.PlaylistUi

internal class PlaylistLoadingWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = VPlaylistLoadingBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    init {
        orientation = VERTICAL
        showDividers = SHOW_DIVIDER_MIDDLE
        dividerDrawable = context.getDrawableOrThrow(R.drawable.space_h_8)
    }

    fun setState(state: PlaylistUi.Loading) {
        binding.titleTv.setTextOrMakeGone(state.title)
    }

    companion object {

        private fun create(context: Context): PlaylistLoadingWidget {
            return PlaylistLoadingWidget(context)
        }

        fun createAdapterDelegate() =
            BaseAdapterDelegate(
                PlaylistUi.Loading::class,
                PlaylistLoadingWidget::create,
                widgetWidth = ViewGroup.LayoutParams.WRAP_CONTENT,
                widgetHeight = ViewGroup.LayoutParams.WRAP_CONTENT
            ) { widget, state, _ ->
                widget.setState(state)
            }

    }

}
