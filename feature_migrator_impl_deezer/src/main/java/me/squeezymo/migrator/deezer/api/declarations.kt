package me.squeezymo.migrator.deezer.api

import android.net.Uri
import me.squeezymo.migrator.deezer.BuildConfig
import me.squeezymo.oauth.api.OAuth2Config

const val DI_DEEZER_OAUTH2_SERVICE_FACTORY = "deezer_oauth2_service_factory"

typealias DeezerPlaylistID = String
typealias DeezerTrackID = String
typealias DeezerArtistID = String
typealias DeezerAlbumID = String

object DeezerConst {

    private val scopes = arrayOf(
        "basic_access",
        "manage_library"
    )

    val oauth2Config = OAuth2Config(
        authServiceConfig = OAuth2Config.AuthServiceConfiguration.Manual(
            authUri = Uri.parse("https://connect.deezer.com/oauth/auth.php"),
            tokenUri = Uri.parse("https://connect.deezer.com/oauth/access_token.php")
        ),
        authResponseType = OAuth2Config.AuthResponseType.TOKEN,
        clientId = BuildConfig.OAUTH_DEEZER_APP_ID,
        redirectUri = Uri.parse(
            "https://filch-a0eb0.firebaseapp.com/oauth2redirect"
        ),
        scopes = scopes,
        additionalAuthParameters = mapOf(
            "app_id" to BuildConfig.OAUTH_DEEZER_APP_ID,
            "perms" to scopes.joinToString(",")
        )
    )

}
