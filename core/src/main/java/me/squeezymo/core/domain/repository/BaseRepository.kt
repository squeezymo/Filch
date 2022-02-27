package me.squeezymo.core.domain.repository

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.core.ext.mapNotNullValuesTo
import okhttp3.ResponseBody

abstract class BaseRepository(
    gsonBuilder: GsonBuilder
) {

    protected val gson: Gson = gsonBuilder.create()

    protected inline fun <reified E : Any> ResponseBody.deserializeAsJsonError(): E {
        return gson.fromJson(string(), E::class.java)
    }

    protected inline fun <reified E : Any> Map<String, Any?>.toObject(): E {
        return gson.fromJson(gson.toJson(this), E::class.java)
    }

    protected open fun extractIds(
        originalServiceID: StreamingServiceID,
        originalTrackID: String?,
        record: Map<String, Any>?
    ): Map<StreamingServiceID, ID> {
        val result = if (originalTrackID == null) {
            mutableMapOf()
        }
        else {
            mutableMapOf(
                originalServiceID to originalTrackID
            )
        }

        record?.mapNotNullValuesTo(result) { (key, value) ->
            if (key == "track") {
                null
            } else {
                value as? String
            }
        }

        return result
    }

}
