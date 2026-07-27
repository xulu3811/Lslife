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

    fun initSession(sessionId: String, toUserId: String) {
        currentSessionId = sessionId
        targetUserId = toUserId
        if (sessionId.isNotEmpty()) {
            loadHistory(sessionId)
            chatRepository.syncOffline(sessionId)
            viewModelScope.launch {
                chatRepository.clearUnreadCount(sessionId)
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
                val bytes = ImageCompressor.compress(context, uri)
                val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", "chat_image.jpg", reqFile)
                val res = lsRepository.uploadImage(part)
                if (res.isSuccess) {
                    val url = res.getOrNull()?.url ?: throw Exception("返回的图片地址为空")
                    sendMessage(url, "image")
                } else {
                    _state.update { it.copy(error = res.exceptionOrNull()?.message ?: "图片上传失败") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "图片上传异常: ${e.message}") }
            } finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }
}
