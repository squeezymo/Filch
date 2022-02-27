package me.squeezymo.usersupport.impl.data.repository.internal

import android.content.Context
import java.util.*

internal class ChatUserIdHolder(
    context: Context
) {

    private val prefs = context.getSharedPreferences("user_id", Context.MODE_PRIVATE)

    fun getUserId(): String {
        return prefs
            .getString("id", null)
            ?: UUID.randomUUID().toString().uppercase().also { uuid ->
                prefs
                    .edit()
                    .putString("id", uuid)
                    .apply()
            }
    }

}
