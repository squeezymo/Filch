package me.squeezymo.core.firebase.ext

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun Query.snapshotFlow(): Flow<QuerySnapshot> {
    return callbackFlow {
        val registration = addSnapshotListener { value, error ->
            if (error != null) {
                throw error
            }

            trySend(checkNotNull(value))
        }

        awaitClose {
            registration.remove()
        }
    }
}
