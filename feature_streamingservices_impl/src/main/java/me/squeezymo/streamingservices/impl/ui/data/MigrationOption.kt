package me.squeezymo.streamingservices.impl.ui.data

internal sealed class MigrationOption {

    object All : MigrationOption()

    object Manual : MigrationOption()

}
