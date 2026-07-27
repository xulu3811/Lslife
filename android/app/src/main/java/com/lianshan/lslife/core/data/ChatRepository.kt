package com.lianshan.lslife.core.data

import com.lianshan.lslife.core.database.ChatSessionDao
import com.lianshan.lslife.core.database.ChatSessionEntity
import com.lianshan.lslife.core.model.ChatMessage
import com.lianshan.lslife.core.model.ChatSession
import com.lianshan.lslife.core.network.ApiService
import com.lianshan.lslife.core.network.RealtimeClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ApiService,
    private val realtimeClient: RealtimeClient,
    private val chatSessionDao: ChatSessionDao,
    private val authRepository: AuthRepository,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val _currentUserIdFlow = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    val unreadCount: StateFlow<Int> = _currentUserIdFlow
        .flatMapLatest { userId ->
            if (userId.isEmpty()) flowOf(0)
            else chatSessionDao.observeTotalUnread(userId)
        }
        .stateIn(
            scope = GlobalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    suspend fun getSessions(): List<ChatSession> {
        val myId = authRepository.cachedMe()?.id ?: authRepository.me().getOrNull()?.id ?: ""
        if (myId.isNotEmpty() && _currentUserIdFlow.value != myId) {
            _currentUserIdFlow.value = myId
        }
        val res = api.chatSessions()
        if (res.code != 0) throw Exception(res.message)
        val sessions = res.data ?: emptyList()
        if (myId.isNotEmpty()) {
            val entities = sessions.map { s ->
                ChatSessionEntity(
                    id = s.id,
                    user1Id = myId,
                    user2Id = s.targetUser?.id ?: "",
                    targetUserId = s.targetUser?.id,
                    targetUserNickname = s.targetUser?.nickname,
                    targetUserAvatar = s.targetUser?.avatar,
                    lastMessage = s.lastMessage,
                    unread1 = s.unread,
                    unread2 = 0,
                    updatedAt = parseDateToLong(s.updatedAt)
                )
            }
            chatSessionDao.upsertSessions(entities)
        }
        return sessions
    }

    suspend fun getMessages(sessionId: String): List<ChatMessage> {
        val res = api.chatMessages(sessionId)
        if (res.code != 0) throw Exception(res.message)
        
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try { clearUnreadCount(sessionId) } catch (e: Exception) {}
        }
        
        return res.data ?: emptyList()
    }

    suspend fun clearUnreadCount(sessionId: String) {
        val myId = authRepository.cachedMe()?.id ?: authRepository.me().getOrNull()?.id ?: return
        chatSessionDao.clearUnread(sessionId, myId)
        realtimeClient.sendReadAck(sessionId)
    }

    suspend fun syncLocalMessageQuietly(msg: ChatMessage) {
        val myId = authRepository.cachedMe()?.id ?: ""
        if (myId.isEmpty()) return
        if (msg.senderId != myId && !msg.isRecalled) {
            val sessions = chatSessionDao.getAllSessions(myId)
            val exists = sessions.any { it.id == msg.sessionId }
            if (exists) {
                chatSessionDao.updateSessionMessage(
                    sessionId = msg.sessionId,
                    currentUserId = myId,
                    lastMsg = if (msg.type == "image") "[图片]" else msg.content,
                    unreadIncrement = if (msg.isOfflineSync) 0 else 1,
                    updatedAt = parseDateToLong(msg.createdAt)
                )
            } else {
                try { getSessions() } catch (e: Exception) {}
            }
        }
    }

    private fun parseDateToLong(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            val cleanStr = dateStr.substringBefore(".").substringBefore("Z")
            format.parse(cleanStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private val sharedIncomingMessages: SharedFlow<ChatMessage> = realtimeClient.events()
        .transform { text ->
            try {
                val obj = json.parseToJsonElement(text).jsonObject
                val event = obj["event"]?.jsonPrimitive?.content
                if (event == "chat_message") {
                    val msgObj = obj["message"]
                    if (msgObj != null) {
                        val msg = json.decodeFromJsonElement(ChatMessage.serializer(), msgObj)
                        emit(msg)
                    }
                } else if (event == "message_recalled") {
                    val msgId = obj["messageId"]?.jsonPrimitive?.content ?: return@transform
                    val sessionId = obj["sessionId"]?.jsonPrimitive?.content ?: ""
                    val senderId = obj["senderId"]?.jsonPrimitive?.content ?: ""
                    emit(ChatMessage(
                        id = msgId,
                        sessionId = sessionId,
                        senderId = senderId,
                        type = "recalled",
                        content = "对方撤回了一条消息",
                        isRecalled = true,
                        createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())
                    ))
                } else if (event == "offline_sync") {
                    val msgsArray = obj["messages"]?.jsonArray
                    msgsArray?.forEach { element ->
                        try {
                            val msg = json.decodeFromJsonElement(ChatMessage.serializer(), element)
                            emit(msg.copy(isOfflineSync = true))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        .retryWhen { cause, attempt ->
            val delayMs = (1000L * (1L shl attempt.toInt().coerceAtMost(5))).coerceAtMost(30000L)
            delay(delayMs)
            true
        }
        .shareIn(
            scope = @OptIn(DelicateCoroutinesApi::class) GlobalScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 0
        )

    fun incomingMessages(): Flow<ChatMessage> {
        return sharedIncomingMessages
    }

    fun sendMessage(toUserId: String, content: String, type: String = "text") {
        realtimeClient.sendChatMessage(toUserId, content, type)
    }

    suspend fun recallMessage(sessionId: String, messageId: String): Result<Unit> {
        realtimeClient.sendRecallMessage(messageId)
        return try {
            val res = api.recallMessage(sessionId, messageId)
            if (res.code == 0) Result.success(Unit) else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun syncOffline(sessionId: String) {
        realtimeClient.sendOfflineSync(sessionId)
    }

    fun incrementUnread() {
        // No-op: Unread counts are strictly observed from Room SSOT via ChatSessionDao
    }

    fun decrementUnread(amount: Int) {
        // No-op: Unread counts are strictly observed from Room SSOT via ChatSessionDao
    }
}
