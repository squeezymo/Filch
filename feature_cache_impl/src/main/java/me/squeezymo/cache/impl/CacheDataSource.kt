package me.squeezymo.cache.impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import me.squeezymo.cache.api.CacheConfig
import me.squeezymo.cache.api.ICacheDataSource
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.firebase.ext.continueWithUnlessNullResult
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class CacheDataSource @Inject constructor(): ICacheDataSource {

    private val firestoreDb = Firebase.firestore

    override suspend fun addOrUpdateRecord(
        cacheConfig: CacheConfig,
        recordId: ID,
        record: Map<String, Any>
    ) {
        return suspendCoroutine { continuation ->
            val collection = cacheConfig.getFirestoreCollection()

            collection
                .document(recordId)
                .set(HashMap(record).also { it.remove(cacheConfig.serviceId) }, SetOptions.merge())
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener(continuation::resumeWithException)
        }
    }

    override suspend fun findRecordById(
        cacheConfig: CacheConfig,
        recordId: ID
    ): Map<String, Any>? {
        return suspendCoroutine { continuation ->
            val collection = cacheConfig.getFirestoreCollection()

            collection
                .document(recordId)
                .get()
                .continueWithUnlessNullResult { documentSnapshot ->
                    if (documentSnapshot?.exists() == true) {
                        documentSnapshot.data
                    }
                    else {
                        null
                    }
                }
                .addOnSuccessListener { record: Map<String, Any>? ->
                    continuation.resume(record)
                }
                .addOnFailureListener(continuation::resumeWithException)
        }
    }

    private fun CacheConfig.getFirestoreCollection(): CollectionReference {
        return firestoreDb.collection(serviceId)
    }

}
