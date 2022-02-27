package me.squeezymo.oauth.api

import me.squeezymo.core.domain.data.StreamingServiceID

interface IOAuth2Broker {

    fun getAllOAuth2Services(
        services: List<StreamingServiceID>? = null
    ): List<IOAuth2Service>

    fun getOAuth2ServiceById(
        id: String
    ): IOAuth2Service?

}
