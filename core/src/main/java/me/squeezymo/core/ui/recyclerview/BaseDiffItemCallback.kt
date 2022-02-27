package me.squeezymo.core.ui.recyclerview

import androidx.recyclerview.widget.DiffUtil

class BaseDiffItemCallback : DiffUtil.ItemCallback<IListItem>() {

    override fun areItemsTheSame(oldItem: IListItem, newItem: IListItem): Boolean =
        oldItem.isItemTheSameAs(newItem)

    override fun areContentsTheSame(oldItem: IListItem, newItem: IListItem): Boolean =
        oldItem.isContentTheSameAs(newItem)

}
