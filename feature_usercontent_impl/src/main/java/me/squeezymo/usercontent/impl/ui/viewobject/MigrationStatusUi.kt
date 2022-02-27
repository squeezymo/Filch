package me.squeezymo.usercontent.impl.ui.viewobject

sealed class MigrationStatusUi {

    object Unknown : MigrationStatusUi()

    data class NotMigrated(
        val isSelected: Boolean
    ) : MigrationStatusUi()

    object InProgress : MigrationStatusUi()

    object Migrated : MigrationStatusUi()

}
