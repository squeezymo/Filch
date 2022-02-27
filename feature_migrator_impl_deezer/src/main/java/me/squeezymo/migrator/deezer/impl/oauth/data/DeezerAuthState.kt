package me.squeezymo.migrator.deezer.impl.oauth.data

import me.squeezymo.oauth.api.AccessToken

internal data class DeezerAuthState(
    val accessToken: AccessToken,
    val receivedMillis: Long,
    val expiresInSeconds: Long
)
