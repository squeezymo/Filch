package me.squeezymo.usercontent.impl.ui.data

internal sealed class PlaylistMigrationState {

    object InProgress : PlaylistMigrationState()

    object PartiallyMigrated : PlaylistMigrationState()

    object Migrated : PlaylistMigrationState()

    object Error : PlaylistMigrationState()

}
