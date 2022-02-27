package me.squeezymo.usersupport.impl.ui.viewobject

import me.squeezymo.core.ui.recyclerview.IListItem

internal sealed class ChatMessageUi : IListItem {

    data class Outgoing(
        val id: String,
        val body: String,
        val isRead: Boolean
    ) : ChatMessageUi() {

        override fun isItemTheSameAs(another: IListItem): Boolean {
            return another is Outgoing && id == another.id
        }

        override fun isContentTheSameAs(another: IListItem): Boolean {
            return this == another
        }

    }

    data class Incoming(
        val id: String,
        val body: String
    ) : ChatMessageUi() {

        override fun isItemTheSameAs(another: IListItem): Boolean {
            return another is Incoming && id == another.id
        }

        override fun isContentTheSameAs(another: IListItem): Boolean {
            return this == another
        }

    }

    sealed class Technical : ChatMessageUi() {

        sealed class Predefined : Technical() {

            object Header : Predefined()

        }

        data class Arbitrary(
            val id: String,
            val body: String
        ) : Technical() {

            override fun isItemTheSameAs(another: IListItem): Boolean {
                return another is Arbitrary && id == another.id
            }

            override fun isContentTheSameAs(another: IListItem): Boolean {
                return this == another
            }

        }

    }

}
