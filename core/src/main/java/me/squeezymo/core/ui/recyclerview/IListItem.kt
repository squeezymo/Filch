package me.squeezymo.core.ui.recyclerview

interface IListItem {

    fun isItemTheSameAs(another: IListItem): Boolean {
        return false
    }

    fun isContentTheSameAs(another: IListItem): Boolean {
        return false
    }

}
