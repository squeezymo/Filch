package me.squeezymo.usercontent.impl.data

sealed class UserContentError {

    object TracksNotRetrieved : UserContentError()

}
