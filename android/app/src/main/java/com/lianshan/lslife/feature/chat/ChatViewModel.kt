package com.lianshan.lslife.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.AuthRepository
import com.lianshan.lslife.core.data.ChatRepository
import com.lianshan.lslife.core.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.lianshan.lslife.feature.publish.ImageCompressor
import com.lianshan.lslife.core.data.LsRepository
import javax.inject.Inject
import android.util.Base64

data class ChatUiState(
    val loading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val error: String? = null,
    val currentUserId: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val lsRepository: LsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state

    private var currentSessionId: String = ""
    private var targetUserId: String = ""

    init {
        viewModelScope.launch {
            authRepository.me().onSuccess { user ->
                _state.update { it.copy(currentUserId = user.id) }
            }
        }

        viewModelScope.launch {
            chatRepository.incomingMessages().collect { msg ->
                if (msg.type == "recalled" || msg.isRecalled) {
                    val recallText = if (msg.senderId == state.value.currentUserId) "您撤回了一条消息" else "对方撤回了一条消息"
                    _state.update {
                        val newMsgs = it.messages.map { m ->
                            if (m.id == msg.id) m.copy(type = "recalled", content = recallText, isRecalled = true) else m
                        }
                        it.copy(messages = newMsgs)
                    }
                } else {
                    if (currentSessionId == "new" && (msg.senderId == targetUserId || msg.senderId == state.value.currentUserId)) {
                        currentSessionId = msg.sessionId
                    }
                    if (msg.sessionId == currentSessionId || currentSessionId.isEmpty()) {
                        _state.update {
                            val newMessages = (it.messages + msg).distinctBy { m -> m.id }
                            it.copy(messages = newMessages)
                        }
                    }
                }
            }
        }
    }

    fun initSession(sessionId: String, toUserId: String, initPostId: String? = null) {
        currentSessionId = sessionId
        targetUserId = toUserId
        if (sessionId.isNotEmpty() && sessionId != "new") {
            loadHistory(sessionId)
            chatRepository.syncOffline(sessionId)
            viewModelScope.launch {
                chatRepository.clearUnreadCount(sessionId)
            }
        }
        if (initPostId != null) {
            sendPostCard(initPostId)
        }
    }

    private fun sendPostCard(postId: String) {
        viewModelScope.launch {
            lsRepository.post(postId).onSuccess { post ->
                val title = post.title.replace("\"", "\\\"").replace("\n", " ")
                val image = post.images.firstOrNull() ?: ""
                val price = post.price ?: 0.0
                val cardJson = """{"id":"$postId","title":"$title","price":$price,"image":"$image"}"""
                sendMessage(cardJson, "post_card")
            }
        }
    }

    private fun loadHistory(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val msgs = chatRepository.getMessages(sessionId)
                _state.update { it.copy(loading = false, messages = msgs) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Failed to load messages") }
            }
        }
    }

    fun sendMessage(content: String, type: String = "text") {
        if (content.isBlank() || targetUserId.isEmpty()) return
        chatRepository.sendMessage(targetUserId, content, type)
    }

    fun sendLocation(lat: Double, lng: Double, name: String, address: String) {
        val json = """{"lat":$lat,"lng":$lng,"name":"$name","address":"$address"}"""
        sendMessage(json, "location")
    }

    fun recallMessage(messageId: String) {
        if (currentSessionId.isEmpty()) return
        viewModelScope.launch {
            // Optimistic UI update
            _state.update {
                val newMsgs = it.messages.map { m ->
                    if (m.id == messageId) m.copy(type = "recalled", content = "您撤回了一条消息", isRecalled = true) else m
                }
                it.copy(messages = newMsgs)
            }
            val res = chatRepository.recallMessage(currentSessionId, messageId)
            if (res.isFailure) {
                _state.update { it.copy(error = res.exceptionOrNull()?.message ?: "消息撤回失败（可能已超过1分钟）") }
                loadHistory(currentSessionId)
            }
        }
    }

    fun sendImage(uri: String) {
        if (targetUserId.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                // Background compression
                val bytes = withContext(Dispatchers.IO) {
                    ImageCompressor.compress(context, uri)
                }
                
                // Convert bytes to Base64 string
                val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                
                // Directly send via WebSocket with base64 prefix
                sendMessage("base64:\$base64String", "image")
                
            } catch (e: Exception) {
                _state.update { it.copy(error = "图片处理发送异常: \${e.message}") }
            } finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }
    fun sendVoice(filePath: String, duration: Int) {
        if (targetUserId.isEmpty()) return
        viewModelScope.launch {
            try {
                val file = java.io.File(filePath)
                if (!file.exists()) return@launch
                val reqFile = okhttp3.RequestBody.create("audio/mp4".toMediaTypeOrNull(), file)
                val part = MultipartBody.Part.createFormData("audio", file.name, reqFile)
                
                lsRepository.uploadAudio(part).onSuccess { res ->
                    val url = res.url
                    val voiceJson = """{"url":"$url","duration":$duration}"""
                    sendMessage(voiceJson, "voice")
                }.onFailure {
                    _state.update { st -> st.copy(error = "语音上传失败: ${it.message}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "语音发送异常: ${e.message}") }
            }
        }
    }
}
