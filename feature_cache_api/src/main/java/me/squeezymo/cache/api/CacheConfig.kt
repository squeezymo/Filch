package me.squeezymo.cache.api

import me.squeezymo.core.domain.data.StreamingServiceID

interface CacheConfig {

    val serviceId: StreamingServiceID

    val idObjectPath: String

}
