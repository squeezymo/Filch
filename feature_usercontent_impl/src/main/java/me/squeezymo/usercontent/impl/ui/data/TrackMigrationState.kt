package me.squeezymo.usercontent.impl.ui.data

internal sealed class TrackMigrationState {

    object InProgress : TrackMigrationState()

    object Migrated : TrackMigrationState()

    object Error : TrackMigrationState()

}
