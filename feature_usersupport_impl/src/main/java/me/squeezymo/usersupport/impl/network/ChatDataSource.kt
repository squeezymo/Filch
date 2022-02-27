package me.squeezymo.usersupport.impl.network

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.squeezymo.analytics.api.data.IUserFlowEvent
import me.squeezymo.core.ext.castOrThrow
import me.squeezymo.core.firebase.data.Cacheable
import me.squeezymo.core.firebase.ext.awaitResult
import me.squeezymo.core.firebase.ext.snapshotFlow
import me.squeezymo.core.meta.FilchEnvironment
import me.squeezymo.usersupport.impl.network.dto.ChatMessageDTO
import java.util.*
import javax.inject.Inject

internal interface IChatDataSource {

    fun getMessagesFlow(
        userId: String
    ): Flow<List<Cacheable<ChatMessageDTO>>>

    suspend fun markMessagesAsRead(
        userId: String,
        messageIds: List<String>
    )

    suspend fun addMessage(
        userId: String,
        message: String,
        userFlowEvents: List<IUserFlowEvent>
    )

}

internal class ChatDataSource @Inject constructor(
    @ApplicationContext context: Context,
    private val environment: FilchEnvironment
) : IChatDataSource {

    // TODO Remove hardcode, pass values from props that are not under version control
    private val firestoreDb = Firebase.firestore(
        FirebaseApp.initializeApp(
            context,
            FirebaseOptions.Builder()
                .setApplicationId("XXX")
                .setGcmSenderId("XXX")
                .setApiKey("XXX")
                .setProjectId("XXX")
                .setStorageBucket("XXX")
                .setDatabaseUrl("XXX")
                .build(),
            "chat"
        )
    )

    override fun getMessagesFlow(
        userId: String
    ): Flow<List<Cacheable<ChatMessageDTO>>> {
        return firestoreDb
            .collection("groups/Filch/chats/$userId/messages")
            .snapshotFlow()
            .map { querySnapshot ->
                querySnapshot
                    .documents
                    .map { messageDocumentSnapshot ->
                        Cacheable(
                            data = ChatMessageDTO(
                                id = messageDocumentSnapshot.id,
                                body = messageDocumentSnapshot["body"].castOrThrow(),
                                isRead = messageDocumentSnapshot["isRead"].castOrThrow(),
                                senderId = messageDocumentSnapshot["senderId"].castOrThrow(),
                                date = messageDocumentSnapshot["date"].castOrThrow()
                            ),
                            isFromCache = messageDocumentSnapshot.metadata.isFromCache
                        )
                    }
            }
    }

    override suspend fun markMessagesAsRead(
        userId: String,
        messageIds: List<String>
    ) {
        if (messageIds.isEmpty()) {
            return
        }

        firestoreDb.runBatch { batch ->
            messageIds.fold(batch) { currentBatch, messageId ->
                currentBatch.update(
                    firestoreDb.document("groups/Filch/chats/$userId/messages/$messageId"),
                    mapOf(
                        "isRead" to true
                    )
                )
            }
        }.awaitResult()
    }

    override suspend fun addMessage(
        userId: String,
        message: String,
        userFlowEvents: List<IUserFlowEvent>
    ) {
        val date = Timestamp(Date())

        val flowDocuments = firestoreDb
            .collection("groups/Filch/chats/$userId/userFlow")
            .get()
            .awaitResult()
            .documents
            .map(DocumentSnapshot::getReference)

        firestoreDb.runBatch { batch ->
            batch
                .set(
                    firestoreDb
                        .document("groups/Filch/chats/$userId"),
                    mapOf(
                        "userInfo" to mapOf<String, Any>(
                            "platform" to environment.platform,
                            "appVersion" to environment.appVersion
                        ),
                        "needRead" to 1,
                        "lastMessage" to message,
                        "lastDate" to date,
                        "supportId" to "SUPPORT",
                        "userId" to userId
                    ),
                    SetOptions.merge()
                )
                .set(
                    firestoreDb
                        .collection("groups/Filch/chats/$userId/messages")
                        .document(),
                    mapOf<String, Any>(
                        "body" to message,
                        "senderId" to userId,
                        "isRead" to false,
                        "date" to date
                    )
                )

            flowDocuments.fold(batch) { currentBatch, flowDocument ->
                currentBatch.delete(flowDocument)
            }

            userFlowEvents.foldIndexed(batch) { index, currentBatch, userFlowEvent ->
                currentBatch.set(
                    firestoreDb
                        .collection("groups/Filch/chats/$userId/userFlow")
                        .document(),
                    mapOf<String, Any>(
                        "message" to userFlowEvent.getMessage(),
                        "type" to userFlowEvent.getType(),
                        "step" to index + 1
                    )
                )
            }
        }.awaitResult()
    }

}
