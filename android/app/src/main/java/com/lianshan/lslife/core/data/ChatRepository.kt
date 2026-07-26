package com.lianshan.lslife.core.data

import com.lianshan.lslife.core.model.ChatMessage
import com.lianshan.lslife.core.model.ChatSession
import com.lianshan.lslife.core.network.ApiService
import com.lianshan.lslife.core.network.RealtimeClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.delay
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    private val _unreadCount = kotlinx.coroutines.flow.MutableStateFlow(0)
    val unreadCount: kotlinx.coroutines.flow.StateFlow<Int> = _unreadCount

    suspend fun getSessions(): List<ChatSession> {
        val res = api.chatSessions()
        if (res.code != 0) throw Exception(res.message)
        val sessions = res.data ?: emptyList()
        _unreadCount.value = sessions.sumOf { it.unread ?: 0 }
        return sessions
    }

    suspend fun getMessages(sessionId: String): List<ChatMessage> {
        val res = api.chatMessages(sessionId)
        if (res.code != 0) throw Exception(res.message)
        
        // Background sync to clear the unread count for this session
        @OptIn(DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch {
            try { getSessions() } catch (e: Exception) {}
        }
        
        return res.data ?: emptyList()
    }

    private val sharedIncomingMessages: SharedFlow<ChatMessage> = realtimeClient.events()
        .mapNotNull { text ->
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
        .retryWhen { cause, attempt ->
            delay(3000)
            true
        }
        .shareIn(
            scope = @OptIn(DelicateCoroutinesApi::class) GlobalScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 0
        )

    /**
     * Listen for incoming chat messages.
     * The backend sends { "event": "chat_message", "message": { ... } }
     */
    fun incomingMessages(): Flow<ChatMessage> {
        return sharedIncomingMessages
    }

    fun sendMessage(toUserId: String, content: String, type: String = "text") {
        realtimeClient.sendChatMessage(toUserId, content, type)
    }

    fun incrementUnread() {
        _unreadCount.value += 1
    }

    fun decrementUnread(amount: Int) {
        val next = _unreadCount.value - amount
        _unreadCount.value = if (next < 0) 0 else next
    }
}
