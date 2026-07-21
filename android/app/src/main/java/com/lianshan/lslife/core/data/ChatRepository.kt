package com.lianshan.lslife.core.data

import com.lianshan.lslife.core.model.ChatMessage
import com.lianshan.lslife.core.model.ChatSession
import com.lianshan.lslife.core.network.ApiService
import com.lianshan.lslife.core.network.RealtimeClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ApiService,
    private val realtimeClient: RealtimeClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getSessions(): List<ChatSession> {
        val res = api.chatSessions()
        if (res.code != 0) throw Exception(res.message)
        return res.data ?: emptyList()
    }

    suspend fun getMessages(sessionId: String): List<ChatMessage> {
        val res = api.chatMessages(sessionId)
        if (res.code != 0) throw Exception(res.message)
        return res.data ?: emptyList()
    }

    /**
     * Listen for incoming chat messages.
     * The backend sends { "event": "chat_message", "message": { ... } }
     */
    fun incomingMessages(): Flow<ChatMessage> {
        return realtimeClient.events().mapNotNull { text ->
            try {
                val obj = json.parseToJsonElement(text).jsonObject
                if (obj["event"]?.jsonPrimitive?.content == "chat_message") {
                    val msgObj = obj["message"]
                    if (msgObj != null) {
                        json.decodeFromJsonElement(ChatMessage.serializer(), msgObj)
                    } else null
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    fun sendMessage(toUserId: String, content: String, type: String = "text") {
        realtimeClient.sendChatMessage(toUserId, content, type)
    }
}
