package me.squeezymo.cache.api

import me.squeezymo.core.domain.data.ID

interface ICacheDataSource {

    suspend fun addOrUpdateRecord(
        cacheConfig: CacheConfig,
        recordId: ID,
        record: Map<String, Any>
    )

    suspend fun findRecordById(
        cacheConfig: CacheConfig,
        recordId: ID
    ) : Map<String, Any>?

}
