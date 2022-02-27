package me.squeezymo.usercontent.impl.ui.utils

import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.progressindicator.LinearProgressIndicator
import me.squeezymo.core.ext.getColorCompat
import me.squeezymo.core.ext.isNullOrZero
import me.squeezymo.core.ui.util.TextHighlighter
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.streamingservices.api.domain.ui.getUiName
import me.squeezymo.usercontent.impl.R
import me.squeezymo.usercontent.impl.ui.uistate.SelectionModeUiState
import me.squeezymo.usercontent.impl.ui.uistate.ShowMigratedTracksUiState

internal object Utils {

    fun formatTrackDuration(durationMillis: Long): String {
        val durationSeconds = durationMillis / 1000

        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        return if (hours == 0L) {
            String.format("%d:%02d", minutes, seconds)
        }
        else {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        }
    }

    @DrawableRes
    fun getServiceIconOn(service: StreamingService): Int {
        return when (service) {
            StreamingService.VK -> TODO()
            StreamingService.APPLE_MUSIC -> TODO()
            StreamingService.YANDEX_MUSIC -> TODO()
            StreamingService.SPOTIFY -> R.drawable.logo_spotify
            StreamingService.DEEZER -> R.drawable.logo_deezer
            StreamingService.YOUTUBE -> TODO()
        }
    }

    @DrawableRes
    fun getServiceIconOff(service: StreamingService): Int {
        return when (service) {
            StreamingService.VK -> TODO()
            StreamingService.APPLE_MUSIC -> TODO()
            StreamingService.YANDEX_MUSIC -> TODO()
            StreamingService.SPOTIFY -> R.drawable.logo_spotify_off
            StreamingService.DEEZER -> R.drawable.logo_deezer_off
            StreamingService.YOUTUBE -> TODO()
        }
    }

    fun updateCounter(
        tracksCounterTv: TextView,
        tracksInSourceService: Int?,
        tracksInDestinationService: Int?
    ) {
        if (tracksInSourceService == null || tracksInDestinationService == null) {
            tracksCounterTv.isInvisible = true
        } else {
            tracksCounterTv.isVisible = true
            tracksCounterTv.text = TextHighlighter(Regex("<b>(.*?)</b>"))
                .createHighlightableText(
                    text = tracksCounterTv.resources.getString(
                        R.string.user_content_header_migrated_counter,
                        tracksInDestinationService,
                        tracksInSourceService
                    ),
                    highlightSpan = {
                        ForegroundColorSpan(tracksCounterTv.context.getColorCompat(R.color.white))
                    }
                )
        }
    }

    fun updateProgress(
        migrationProgressPi: LinearProgressIndicator,
        progress: Int
    ) {
        migrationProgressPi.setProgressCompat(progress, true)
    }

    fun updateSelectionModeButton(
        selectTracksBtn: TextView,
        state: SelectionModeUiState
    ) {
        when (state) {
            SelectionModeUiState.None -> {
                selectTracksBtn.isGone = true
            }
            SelectionModeUiState.Off -> {
                selectTracksBtn.text = selectTracksBtn.resources.getString(R.string.user_content_header_enable_selection)
                selectTracksBtn.isVisible = true
            }
            SelectionModeUiState.On -> {
                selectTracksBtn.text = selectTracksBtn.resources.getString(R.string.user_content_header_cancel_selection)
                selectTracksBtn.isVisible = true
            }
        }
    }

    fun updateShowMigratedTracks(
        toggleShowMigratedTracksBtn: ImageView,
        state: ShowMigratedTracksUiState,
        service: StreamingService
    ) {
        when (state) {
            ShowMigratedTracksUiState.None -> {
                toggleShowMigratedTracksBtn.isGone = true
            }
            ShowMigratedTracksUiState.Hidden -> {
                toggleShowMigratedTracksBtn.setImageResource(
                    getServiceIconOff(service)
                )
                toggleShowMigratedTracksBtn.isVisible = true
            }
            ShowMigratedTracksUiState.Shown -> {
                toggleShowMigratedTracksBtn.setImageResource(
                    getServiceIconOn(service)
                )
                toggleShowMigratedTracksBtn.isVisible = true
            }
        }
    }

    fun updateMigrateButton(
        migrateBtn: Button,
        searchInProgress: Boolean,
        tracksToMigrate: Int?,
        playlistsToMigrate: Int?,
        service: StreamingService
    ) {
        val hasNoTracksToMigrate = tracksToMigrate.isNullOrZero()
        val hasNoPlaylistsToMigrate = playlistsToMigrate.isNullOrZero()
        val mustHide = searchInProgress || (hasNoTracksToMigrate && hasNoPlaylistsToMigrate)

        if ((migrateBtn.isVisible && mustHide) || (!migrateBtn.isVisible && !mustHide)) {
            val parent = migrateBtn.parent as? ViewGroup

            if (parent != null) {
                TransitionManager.beginDelayedTransition(
                    parent,
                    Slide().apply {
                        addTarget(migrateBtn)
                    }
                )
            }
        }

        if (mustHide) {
            migrateBtn.isGone = true
        }
        else {
            android.util.Log.d("333444", "tracksToMigrate=${tracksToMigrate}, isNullOrZero=${tracksToMigrate.isNullOrZero()}")
            android.util.Log.d("333444", "playlistsToMigrate=${playlistsToMigrate}, isNullOrZero=${playlistsToMigrate.isNullOrZero()}")

            val migrateBtnText =
                when {
                    !tracksToMigrate.isNullOrZero() && playlistsToMigrate.isNullOrZero() -> {
                        migrateBtn.resources.getString(
                            R.string.user_content_action_migrate_one_entity,
                            migrateBtn.resources.getQuantityString(
                                R.plurals.user_content_tracks_quantity,
                                tracksToMigrate,
                                tracksToMigrate.toString()
                            ),
                            service.getUiName(migrateBtn.resources)
                        )
                    }
                    tracksToMigrate.isNullOrZero() && !playlistsToMigrate.isNullOrZero() -> {
                        migrateBtn.resources.getString(
                            R.string.user_content_action_migrate_one_entity,
                            migrateBtn.resources.getQuantityString(
                                R.plurals.user_content_playlists_quantity,
                                playlistsToMigrate,
                                playlistsToMigrate.toString()
                            ),
                            service.getUiName(migrateBtn.resources)
                        )
                    }
                    !tracksToMigrate.isNullOrZero() && !playlistsToMigrate.isNullOrZero() -> {
                        migrateBtn.resources.getString(
                            R.string.user_content_action_migrate_two_entities,
                            migrateBtn.resources.getQuantityString(
                                R.plurals.user_content_playlists_quantity,
                                playlistsToMigrate,
                                playlistsToMigrate.toString()
                            ),
                            migrateBtn.resources.getQuantityString(
                                R.plurals.user_content_tracks_quantity,
                                tracksToMigrate,
                                tracksToMigrate.toString()
                            ),
                            service.getUiName(migrateBtn.resources)
                        )
                    }
                    else -> {
                        null
                    }
                }

            if (migrateBtnText == null) {
                migrateBtn.isGone = true
            }
            else {
                migrateBtn.text = migrateBtnText
                migrateBtn.isVisible = true
            }
        }
    }

}
