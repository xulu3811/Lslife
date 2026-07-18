package com.lianshan.lslife.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Merchant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val merchants: List<Merchant> = emptyList(),
    val recommended: List<Merchant> = emptyList(),
    val query: String = "",
    val category: String = "all",
    val sort: String = "default",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: LsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    private var searchJob: Job? = null

    init {
        load()
    }

    fun onQueryChange(v: String) {
        _state.update { it.copy(query = v) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            load(showFullLoading = false)
        }
    }

    fun onCategory(c: String) {
        _state.update { it.copy(category = c) }
        load()
    }

    fun onSort(s: String) {
        _state.update { it.copy(sort = s) }
        load()
    }

    fun load(showFullLoading: Boolean = true) {
        val s = _state.value
        viewModelScope.launch {
            if (showFullLoading) {
                _state.update { it.copy(loading = true, error = null) }
            }
            repo.merchants(s.category, s.query, s.sort)
                .onSuccess { page -> _state.update { it.copy(loading = false, merchants = page.list, error = null) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message ?: "加载失败") } }
            repo.recommended().onSuccess { rec -> _state.update { it.copy(recommended = rec) } }
        }
    }
}
