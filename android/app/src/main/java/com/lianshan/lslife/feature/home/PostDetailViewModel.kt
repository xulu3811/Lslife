package com.lianshan.lslife.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailState(
    val loading: Boolean = false,
    val post: Post? = null,
    val error: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: LsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PostDetailState())
    val state = _state.asStateFlow()

    fun loadPost(id: String) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val res = repository.post(id)
            if (res.isSuccess) {
                _state.update { it.copy(loading = false, post = res.getOrNull()) }
            } else {
                _state.update { it.copy(loading = false, error = res.exceptionOrNull()?.message ?: "加载失败") }
            }
        }
    }
}
