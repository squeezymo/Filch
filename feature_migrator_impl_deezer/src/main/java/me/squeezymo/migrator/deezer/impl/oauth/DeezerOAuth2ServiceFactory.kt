package me.squeezymo.migrator.deezer.impl.oauth

import dagger.assisted.AssistedFactory
import me.squeezymo.migrator.deezer.impl.oauth.DeezerOAuth2Service
import me.squeezymo.oauth.api.IOAuth2Service
import me.squeezymo.oauth.api.IOAuth2ServiceFactory
import me.squeezymo.oauth.api.OAuth2Config
import javax.inject.Inject

@AssistedFactory
internal interface DeezerOAuth2ServiceAssistedFactory {

    fun create(id: String, config: OAuth2Config): DeezerOAuth2Service

}

internal class DeezerOAuth2ServiceFactory @Inject constructor(
    private val assistedFactory: DeezerOAuth2ServiceAssistedFactory
): IOAuth2ServiceFactory {

    override fun create(
        id: String,
        config: OAuth2Config
    ): IOAuth2Service {
        return assistedFactory.create(id, config)
    }

}
