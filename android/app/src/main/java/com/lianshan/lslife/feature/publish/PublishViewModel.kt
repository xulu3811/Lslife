package com.lianshan.lslife.feature.publish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Quota
import com.lianshan.lslife.core.network.CreatePostRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublishUiState(
    val category: String = "second_hand",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val quota: Quota? = null,
    val submitting: Boolean = false,
    val message: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class PublishViewModel @Inject constructor(
    private val repo: LsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PublishUiState())
    val state: StateFlow<PublishUiState> = _state

    fun loadQuota() {
        viewModelScope.launch {
            repo.quota().onSuccess { q -> _state.update { it.copy(quota = q) } }
        }
    }

    fun onCategory(c: String) = _state.update { it.copy(category = c) }
    fun onTitle(v: String) = _state.update { it.copy(title = v) }
    fun onDescription(v: String) = _state.update { it.copy(description = v) }
    fun onPrice(v: String) = _state.update { it.copy(price = v.filter { c -> c.isDigit() || c == '.' }) }
    fun clearMessage() = _state.update { it.copy(message = null, success = false) }

    fun submit() {
        val s = _state.value
        if (s.title.isBlank() || s.description.isBlank()) {
            _state.update { it.copy(message = "请填写标题和描述") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            repo.createPost(
                CreatePostRequest(
                    category = s.category,
                    title = s.title,
                    description = s.description,
                    price = s.price.toDoubleOrNull(),
                ),
            ).onSuccess {
                _state.update { PublishUiState(message = "发布成功", success = true) }
                loadQuota()
            }.onFailure { e ->
                _state.update { it.copy(submitting = false, message = e.message ?: "发布失败") }
            }
        }
    }
}
