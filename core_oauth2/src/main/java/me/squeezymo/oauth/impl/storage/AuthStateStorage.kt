package me.squeezymo.oauth.impl.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import me.squeezymo.core.ext.editAndApply
import net.openid.appauth.AuthState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AuthStateStorage @Inject constructor(
    @ApplicationContext appContext: Context
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

    fun readAuthState(key: String): AuthState? {
        return prefs
            .getString(key, null)
            ?.let(AuthState::jsonDeserialize)
    }

    fun writeAuthState(key: String, state: AuthState?) {
        prefs.editAndApply {
            if (state == null) {
                remove(key)
            }
            else {
                putString(key, state.jsonSerializeString())
            }
        }
    }

}
