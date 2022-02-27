package me.squeezymo.usercontent.impl.ui.mapper

import dagger.hilt.android.scopes.ViewModelScoped
import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.core.domain.data.ID
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.impl.ui.utils.Utils
import me.squeezymo.usercontent.impl.ui.viewobject.MigrationStatusUi
import me.squeezymo.usercontent.impl.ui.viewobject.TrackThumbnailUi
import me.squeezymo.usercontent.impl.ui.viewobject.UserContentItemUi
import javax.inject.Inject

@ViewModelScoped
internal class TrackUiMapper @Inject constructor() {

    fun mapTrack(
        id: ID,
        track: BaseTrack,
        isInSelectionMode: Boolean,
        migrationStatus: MigrationStatusUi,
        dstService: StreamingService
    ): UserContentItemUi.Track {
        return UserContentItemUi.Track(
            id = id,
            title = track.title,
            artist = track.artist,
            audioPreviewUrl = track.audioPreviewUrl,
            duration = track.durationMillis?.let(Utils::formatTrackDuration),
            thumbnail = TrackThumbnailUi(
                hasAudioPreview = track.audioPreviewUrl != null,
                thumbnailUrl = track.thumbnailUrl
            ),
            isInSelectionMode = isInSelectionMode,
            migrationStatus = migrationStatus,
            dstService = dstService
        )
    }

}
