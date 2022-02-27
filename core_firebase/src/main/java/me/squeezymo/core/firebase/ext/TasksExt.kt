package me.squeezymo.core.firebase.ext

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <I> Task<I>.awaitResult(): I {
    return suspendCoroutine { cont ->
        this
            .addOnSuccessListener { result ->
                cont.resume(result)
            }
            .addOnFailureListener { exception ->
                cont.resumeWithException(exception)
            }
    }
}


fun <I, O> Task<I>.continueWithUnlessNullResult(
    map: (I) -> O
): Task<O> {
    return continueWithTask { task ->
        if (task.isCanceled) {
            Tasks.forCanceled()
        } else {
            val exception = task.exception

            if (exception != null) {
                Tasks.forException(exception)
            } else {
                Tasks.forResult(map(requireNotNull(task.result)))
            }
        }
    }
}

fun <I, O> Task<I>.continueWithTaskUnlessNullResult(
    map: (I) -> Task<O>
): Task<O> {
    return continueWithTask { task ->
        if (task.isCanceled) {
            Tasks.forCanceled()
        } else {
            val exception = task.exception

            if (exception != null) {
                Tasks.forException(exception)
            } else {
                map(requireNotNull(task.result))
            }
        }
    }
}
