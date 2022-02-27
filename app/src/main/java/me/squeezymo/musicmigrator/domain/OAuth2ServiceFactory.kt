package me.squeezymo.musicmigrator.domain

import me.squeezymo.migrator.deezer.api.DI_DEEZER_OAUTH2_SERVICE_FACTORY
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.oauth.api.DI_DEFAULT_OAUTH2_SERVICE_FACTORY
import me.squeezymo.oauth.api.IOAuth2Service
import me.squeezymo.oauth.api.IOAuth2ServiceFactory
import me.squeezymo.oauth.api.OAuth2Config
import javax.inject.Inject
import javax.inject.Named

internal class OAuth2ServiceFactory @Inject constructor(
    @Named(DI_DEEZER_OAUTH2_SERVICE_FACTORY) private val deezerFactory: IOAuth2ServiceFactory,
    @Named(DI_DEFAULT_OAUTH2_SERVICE_FACTORY) private val defaultFactory: IOAuth2ServiceFactory
): IOAuth2ServiceFactory {

    override fun create(
        id: String,
        config: OAuth2Config
    ): IOAuth2Service {
        return when (id) {
            StreamingService.DEEZER.id -> deezerFactory.create(id, config)
            else -> defaultFactory.create(id, config)
        }
    }

}
