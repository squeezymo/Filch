package me.squeezymo.oauth.api

interface IOAuth2ServiceFactory {

    fun create(
        id: String,
        config: OAuth2Config
    ): IOAuth2Service

}
