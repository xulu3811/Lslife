package com.lianshan.lslife.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.AuthRepository
import com.lianshan.lslife.core.data.ChatRepository
import com.lianshan.lslife.core.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val authRepository: AuthRepository
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
                if (msg.sessionId == currentSessionId || currentSessionId.isEmpty()) {
                    _state.update {
                        val newMessages = (it.messages + msg).distinctBy { m -> m.id }
                        it.copy(messages = newMessages)
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

    fun sendMessage(content: String) {
        if (content.isBlank() || targetUserId.isEmpty()) return
        chatRepository.sendMessage(targetUserId, content)
        // Optimistic UI update could be done here if needed
    }
}
