package me.squeezymo.usercontent.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import me.squeezymo.core.ext.clipWithRoundRect
import me.squeezymo.core.ext.dp
import me.squeezymo.core.ext.visibleOrGone
import me.squeezymo.usercontent.impl.R
import me.squeezymo.usercontent.impl.databinding.VTrackThumbnailBinding
import me.squeezymo.usercontent.impl.ui.data.PlaybackState
import me.squeezymo.usercontent.impl.ui.viewobject.TrackThumbnailUi

internal class TrackThumbnailWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = VTrackThumbnailBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    init {
        setBackgroundResource(R.color.white_a50)
        clipWithRoundRect(dp(4))
    }

    fun setState(state: TrackThumbnailUi) {
        if (state.thumbnailUrl == null) {
            Glide
                .with(this)
                .clear(binding.thumbnailIv)
        } else {
            Glide
                .with(this)
                .load(state.thumbnailUrl)
                .into(binding.thumbnailIv)
        }

        binding.previewIv.visibleOrGone { state.hasAudioPreview }
    }

    fun setPlaybackState(state: PlaybackState?) {
        when (state) {
            PlaybackState.Preparing,
            PlaybackState.Resumed -> {
                binding.previewIv.setImageResource(R.drawable.ic_pause)
            }
            null,
            PlaybackState.Prepared,
            PlaybackState.Paused -> {
                binding.previewIv.setImageResource(R.drawable.ic_play)
            }
        }
    }

    fun setDimmed(isDimmed: Boolean) {
        binding.dimView.isVisible = isDimmed
    }

}
