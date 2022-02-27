package me.squeezymo.oauth.api.exception

import java.lang.RuntimeException

class NoAccessTokenException(
    val serviceId: String
) : RuntimeException() {

    override val message: String
        get() = "Access token has either expired or is inaccessible (serviceId=\"$serviceId\")"

}