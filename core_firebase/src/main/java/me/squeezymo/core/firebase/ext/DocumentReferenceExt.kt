package me.squeezymo.core.firebase.ext

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun DocumentReference.snapshotFlow(): Flow<DocumentSnapshot> {
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
