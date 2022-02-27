package me.squeezymo.musicmigrator.domain

import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.core.ext.filterNotNullValues
import me.squeezymo.musicmigrator.domain.usecase.internal.IOAuth2ConfigFactory
import me.squeezymo.oauth.api.IOAuth2Broker
import me.squeezymo.oauth.api.IOAuth2Service
import me.squeezymo.oauth.api.IOAuth2ServiceFactory
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import javax.inject.Inject

internal class OAuth2Broker @Inject constructor(
    private val oAuth2ConfigFactory: IOAuth2ConfigFactory,
    private val oAuth2ServiceFactory: IOAuth2ServiceFactory
) : IOAuth2Broker {

    private val oAuth2ServiceByStreamingService: Map<StreamingService, IOAuth2Service> =
        StreamingService
            .values()
            .filter(StreamingService::isEnabled)
            .associateWith(oAuth2ConfigFactory::getOAuth2ConfigByStreamingService)
            .filterNotNullValues()
            .mapValues { (streamingService, oAuth2Config) ->
                oAuth2ServiceFactory.create(streamingService.id, oAuth2Config)
            }

    override fun getAllOAuth2Services(
        services: List<StreamingServiceID>?
    ): List<IOAuth2Service> {
        return oAuth2ServiceByStreamingService
            .values
            .filter { oAuth2Service ->
                services == null || services.contains(oAuth2Service.serviceId)
            }
    }

    override fun getOAuth2ServiceById(
        id: String
    ): IOAuth2Service? {
        val streamingService = StreamingService.findById(id) ?: return null
        return oAuth2ServiceByStreamingService[streamingService]
    }

}
