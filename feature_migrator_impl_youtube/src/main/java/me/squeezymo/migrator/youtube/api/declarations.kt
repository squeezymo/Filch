package me.squeezymo.migrator.youtube.api

import android.net.Uri
import me.squeezymo.migrator.youtube.BuildConfig
import me.squeezymo.oauth.api.OAuth2Config

typealias YouTubePlaylistID = String

object YouTubeConst {

    val oauth2Config = OAuth2Config(
        authServiceConfig = OAuth2Config.AuthServiceConfiguration.OpenIdConnectIssuer(
            uri = Uri.parse("https://accounts.google.com")
        ),
        authResponseType = OAuth2Config.AuthResponseType.CODE,
        clientId = BuildConfig.OAUTH_GOOGLE_CLIENT_ID,
        redirectUri = Uri.parse("${BuildConfig.OAUTH_GOOGLE_REDIRECT_URI_SCHEME}:/oauth2redirect"),
        scopes = arrayOf(
            Scope.youtube
        )
    )

    private object Scope {
        const val youtube = "https://www.googleapis.com/auth/youtube"
    }

}
