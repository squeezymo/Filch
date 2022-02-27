package me.squeezymo.oauth.api

import android.net.Uri

class OAuth2Config(
    val authServiceConfig: AuthServiceConfiguration,
    val authResponseType: AuthResponseType,
    val clientId: String,
    val redirectUri: Uri,
    val scopes: Array<String>,
    val additionalAuthParameters: Map<String, String>? = null
) {

    enum class AuthResponseType(val value: String) {
        CODE("code"),
        TOKEN("token"),
        ID_TOKEN("id_token")
    }

    sealed class AuthServiceConfiguration {

        class OpenIdConnectIssuer(
            val uri: Uri
        ) : AuthServiceConfiguration()

        class Manual(
            val authUri: Uri,
            val tokenUri: Uri
        ) : AuthServiceConfiguration()

    }

}
