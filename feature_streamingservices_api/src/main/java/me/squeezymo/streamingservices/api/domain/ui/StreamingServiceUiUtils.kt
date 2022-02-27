package me.squeezymo.streamingservices.api.domain.ui

import android.content.Context
import android.content.res.Resources
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import me.squeezymo.core.ext.getColorCompat
import me.squeezymo.streamingservices.api.R
import me.squeezymo.streamingservices.api.domain.data.StreamingService

fun StreamingService.getUiName(resources: Resources): String {
    return when (this) {
        StreamingService.VK -> {
            resources.getString(R.string.vk)
        }
        StreamingService.APPLE_MUSIC -> {
            resources.getString(R.string.apple_music)
        }
        StreamingService.YANDEX_MUSIC -> {
            resources.getString(R.string.yandex_music)
        }
        StreamingService.SPOTIFY -> {
            resources.getString(R.string.spotify)
        }
        StreamingService.DEEZER -> {
            resources.getString(R.string.deezer)
        }
        StreamingService.YOUTUBE -> {
            resources.getString(R.string.youtube_music)
        }
    }
}

@ColorInt
fun StreamingService.getMainColor(
    context: Context
): Int {
    return when (this) {
        StreamingService.VK -> {
            TODO()
        }
        StreamingService.APPLE_MUSIC -> {
            TODO()
        }
        StreamingService.YANDEX_MUSIC -> {
            TODO()
        }
        StreamingService.SPOTIFY -> context.getColorCompat(R.color.spotify_main)
        StreamingService.DEEZER -> context.getColorCompat(R.color.deezer_main)
        StreamingService.YOUTUBE -> context.getColorCompat(R.color.youtube_music_main)
    }
}

fun StreamingService.getUiMigrationAction(resources: Resources): String {
    return when (this) {
        StreamingService.VK -> resources.getString(R.string.migrate_to_vk)
        StreamingService.APPLE_MUSIC -> resources.getString(R.string.migrate_to_apple_music)
        StreamingService.YANDEX_MUSIC -> resources.getString(R.string.migrate_to_yandex_music)
        StreamingService.SPOTIFY -> resources.getString(R.string.migrate_to_spotify)
        StreamingService.DEEZER -> resources.getString(R.string.migrate_to_deezer)
        StreamingService.YOUTUBE -> resources.getString(R.string.migrate_to_youtube_music)
    }
}

@DrawableRes
fun StreamingService.getIconResId(): Int {
    return when (this) {
        StreamingService.VK -> {
            // TODO
            0
        }
        StreamingService.APPLE_MUSIC -> {
            // TODO
            0
        }
        StreamingService.YANDEX_MUSIC -> {
            // TODO
            0
        }
        StreamingService.SPOTIFY -> R.drawable.logo_spotify
        StreamingService.DEEZER -> R.drawable.logo_deezer
        StreamingService.YOUTUBE -> R.drawable.logo_youtube_music
    }
}

@DrawableRes
fun StreamingService.getBottomSheetBackgroundResId(): Int {
    return when (this) {
        StreamingService.VK -> {
            // TODO
            0
        }
        StreamingService.APPLE_MUSIC -> {
            // TODO
            0
        }
        StreamingService.YANDEX_MUSIC -> {
            // TODO
            0
        }
        StreamingService.SPOTIFY -> R.drawable.bs_bg_spotify
        StreamingService.DEEZER -> R.drawable.bs_bg_deezer
        StreamingService.YOUTUBE -> R.drawable.bs_bg_youtube_music
    }
}
