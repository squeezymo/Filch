package me.squeezymo.oauth.impl

import dagger.assisted.AssistedFactory
import me.squeezymo.oauth.api.IOAuth2Service
import me.squeezymo.oauth.api.IOAuth2ServiceFactory
import me.squeezymo.oauth.api.OAuth2Config
import javax.inject.Inject

@AssistedFactory
internal interface OAuth2ServiceAssistedFactory {

    fun create(id: String, config: OAuth2Config): OAuth2Service

}

internal class OAuth2ServiceFactory @Inject constructor(
    private val assistedFactory: OAuth2ServiceAssistedFactory
): IOAuth2ServiceFactory {

    override fun create(
        id: String,
        config: OAuth2Config
    ): IOAuth2Service {
        return assistedFactory.create(id, config)
    }

}
