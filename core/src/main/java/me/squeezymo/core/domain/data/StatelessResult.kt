package me.squeezymo.core.domain.data

sealed class StatelessResult {

    object Success : StatelessResult()

    object Error : StatelessResult()

}
