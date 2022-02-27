package me.squeezymo.migrator.deezer.impl.oauth.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import me.squeezymo.core.ext.editAndApply
import me.squeezymo.migrator.deezer.impl.oauth.data.DeezerAuthState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AuthStateStorage @Inject constructor(
    @ApplicationContext appContext: Context,
    gsonBuilder: GsonBuilder
) {

    private val prefs = EncryptedSharedPreferences.create(
        appContext,
        "oauth2",
        MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = gsonBuilder.create()

    fun readAuthState(key: String): DeezerAuthState? {
        return prefs
            .getString(key, null)
            ?.let { json ->
                gson.fromJson(json, DeezerAuthState::class.java)
            }
    }

    fun writeAuthState(key: String, state: DeezerAuthState?) {
        prefs.editAndApply {
            if (state == null) {
                remove(key)
            }
            else {
                putString(key, gson.toJson(state))
            }
        }
    }

}
