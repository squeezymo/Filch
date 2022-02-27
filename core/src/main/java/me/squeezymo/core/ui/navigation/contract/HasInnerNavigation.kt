package me.squeezymo.core.ui.navigation.contract

interface HasInnerNavigation {

    fun canNavigateUp(): Boolean

    fun navigateUp()

}
