package me.squeezymo.core.domain.data

sealed class Result<S : Any, E : Any> {

    data class Success<S : Any, E : Any>(
        val instance: S
    ) : Result<S, E>()

    data class Error<S : Any, E : Any>(
        val error: E
    ) : Result<S, E>()

    inline fun <SC : Any> map(
        successConverter: S.() -> SC
    ): Result<SC, E> {
        return map(successConverter, { this })
    }

    inline fun <SC : Any, EC : Any> map(
        successConverter: S.() -> SC,
        errorConverter: E.() -> EC
    ): Result<SC, EC> {
        return when (this) {
            is Success -> Success(successConverter(instance))
            is Error -> Error(errorConverter(error))
        }
    }

    inline fun <SC : Any, EC : Any> flatMap(
        successConverter: S.() -> Result<SC, E>
    ): Result<SC, E> {
        return when (this) {
            is Success -> successConverter(instance)
            is Error -> Error(error)
        }
    }

    inline fun <SC : Any, EC : Any> flatMap(
        successConverter: S.() -> Result<SC, EC>,
        errorConverter: E.() -> Result<SC, EC>
    ): Result<SC, EC> {
        return when (this) {
            is Success -> successConverter(instance)
            is Error -> errorConverter(error)
        }
    }

}
