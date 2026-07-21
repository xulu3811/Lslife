package com.lianshan.lslife.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.ChatRepository
import com.lianshan.lslife.core.model.ChatSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatSessionListUiState(
    val loading: Boolean = false,
    val sessions: List<ChatSession> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ChatSessionListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ChatSessionListUiState())
    val state: StateFlow<ChatSessionListUiState> = _state

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val sessions = chatRepository.getSessions()
                _state.update { it.copy(loading = false, sessions = sessions) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Failed to load sessions") }
            }
        }
    }
}
