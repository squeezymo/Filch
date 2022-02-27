package me.squeezymo.migrator.spotify.api

import android.net.Uri
import me.squeezymo.migrator.spotify.BuildConfig
import me.squeezymo.oauth.api.OAuth2Config

typealias SpotifyUserID = String
typealias SpotifyPlaylistID = String
typealias SpotifyTrackID = String
typealias SpotifyArtistID = String
typealias SpotifyAlbumID = String

object SpotifyConst {

    val oauth2Config = OAuth2Config(
        authServiceConfig = OAuth2Config.AuthServiceConfiguration.OpenIdConnectIssuer(
            uri = Uri.parse("https://accounts.spotify.com")
        ),
        authResponseType = OAuth2Config.AuthResponseType.CODE,
        clientId = BuildConfig.OAUTH_SPOTIFY_CLIENT_ID,
        redirectUri = Uri.parse("filch-a0eb0.firebaseapp.com:/oauth2redirect"),
        scopes = arrayOf(
            Scope.playlistModifyPrivate,
            Scope.playlistModifyPublic,
            Scope.playlistReadCollaborative,
            Scope.playlistReadPrivate,
            Scope.userLibraryRead,
            Scope.userLibraryModify,
            Scope.userReadPrivate,
            Scope.userReadPlaybackState,
            Scope.userReadCurrentlyPlaying,
            Scope.userModifyPlaybackState,
            Scope.streaming,
            Scope.userReadTop
        )
    )

    private object Scope {
        /// Read access to user's private playlists.
        const val playlistReadPrivate = "playlist-read-private"

        /// Include collaborative playlists when requesting a user's playlists.
        const val playlistReadCollaborative = "playlist-read-collaborative"

        /// Write access to a user's public playlists.
        const val playlistModifyPublic = "playlist-modify-public"

        /// Write access to a user's private playlists.
        const val playlistModifyPrivate = "playlist-modify-private"

        /// Control playback of a Spotify track. This scope is currently only available to
        // Spotify native SDKs (for example, the iOS SDK and the Android SDK). The user must
        // have a Spotify Premium account.
        const val streaming = "streaming"

        /// Write/delete access to the list of artists and other users that the user follows.
        const val userFollowModify = "user-follow-modify"

        /// Read access to the list of artists and other users that the user follows.
        const val userFollowRead = "user-follow-read"

        /// Write/delete access to a user's "Your Music" library.
        const val userLibraryModify = "user-library-modify"

        /// Read access to a user's "Your Music" library.
        const val userLibraryRead = "user-library-read"

        /// Read access to user’s subscription details (type of user account).
        const val userReadPrivate = "user-read-private"

        /// Read access to the user's birthdate.
        const val userReadBirthDate = "user-read-birthdate"

        /// Read access to user’s email address.
        const val userReadEmail = "user-read-email"

        /// Read access to a user's top artists and tracks.
        const val userReadTop = "user-top-read"

        /// Read access to read the player's playback state
        const val userReadPlaybackState = "user-read-playback-state"

        /// Control the player's playback state
        const val userModifyPlaybackState = "user-modify-playback-state"

        /// Read access to user's currently playing track
        const val userReadCurrentlyPlaying = "user-read-currently-playing"
    }

}
