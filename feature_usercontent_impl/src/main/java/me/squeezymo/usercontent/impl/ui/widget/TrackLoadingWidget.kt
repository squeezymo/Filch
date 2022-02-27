package me.squeezymo.usercontent.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.facebook.shimmer.ShimmerFrameLayout
import me.squeezymo.core.ui.recyclerview.BaseAdapterDelegate
import me.squeezymo.usercontent.impl.databinding.VTrackLoadingBinding
import me.squeezymo.usercontent.impl.ui.viewobject.UserContentItemUi

internal class TrackLoadingWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShimmerFrameLayout(context, attrs, defStyleAttr) {

    private val binding = VTrackLoadingBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    init {

    }

    fun setState(state: UserContentItemUi.TracksLoading) {
        /* do nothing */
    }

    companion object {

        private fun create(context: Context): TrackLoadingWidget {
            return TrackLoadingWidget(context)
        }

        fun createAdapterDelegate() =
            BaseAdapterDelegate(
                UserContentItemUi.TracksLoading::class,
                TrackLoadingWidget::create
            ) { widget, state, _ ->
                widget.setState(state)
            }

    }

}
