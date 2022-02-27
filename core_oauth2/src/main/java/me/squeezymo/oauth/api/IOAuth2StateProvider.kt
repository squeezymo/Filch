package me.squeezymo.oauth.api

import me.squeezymo.core.domain.data.StreamingServiceID

interface IOAuth2StateProvider {

    val serviceId: StreamingServiceID

    suspend fun getFreshAccessState(): OAuth2State?

}
