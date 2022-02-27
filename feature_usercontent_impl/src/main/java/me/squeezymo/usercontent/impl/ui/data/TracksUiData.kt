package me.squeezymo.usercontent.impl.ui.data

import me.squeezymo.core.domain.data.ID

internal sealed class TracksUiData {

    object Hide : TracksUiData()

    data class Show(
        val tracksSearchState: TracksSearchState?,
        val tracksMigrationState: Map<ID, TrackMigrationState>,
        val tracksSelectedForMigration: Set<ID>
    ): TracksUiData()

}
