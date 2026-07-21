package com.lianshan.lslife.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val keyword: String = "",
    val category: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sortBy: String = "latest", // latest, price_asc, price_desc
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val page: Int = 1,
    val posts: List<Post> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: LsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state

    private var searchJob: Job? = null

    fun updateKeyword(k: String) {
        _state.update { it.copy(keyword = k) }
        debouncedSearch()
    }

    fun updateCategory(c: String?) {
        _state.update { it.copy(category = c) }
        load(page = 1)
    }

    fun updatePrice(min: Double?, max: Double?) {
        _state.update { it.copy(minPrice = min, maxPrice = max) }
        load(page = 1)
    }

    fun updateSort(sort: String) {
        _state.update { it.copy(sortBy = sort) }
        load(page = 1)
    }

    private fun debouncedSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            load(page = 1, showFullLoading = false)
        }
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true) }
        load(page = 1, showFullLoading = false)
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.loadingMore || !s.hasMore) return
        _state.update { it.copy(loadingMore = true) }
        load(page = s.page + 1, showFullLoading = false)
    }

    private fun load(page: Int, showFullLoading: Boolean = true) {
        val s = _state.value
        viewModelScope.launch {
            if (showFullLoading) {
                _state.update { it.copy(loading = true, error = null) }
            }
            if (page == 1 && showFullLoading) {
                _state.update { it.copy(posts = emptyList()) }
            }

            repo.posts(
                category = s.category,
                mine = false,
                q = s.keyword.takeIf { it.isNotBlank() },
                minPrice = s.minPrice,
                maxPrice = s.maxPrice,
                sortBy = s.sortBy,
                page = page,
                pageSize = 20
            ).onSuccess { resPage ->
                _state.update {
                    val newPosts = if (page == 1) resPage.list else it.posts + resPage.list
                    val hasMore = resPage.page * resPage.pageSize < resPage.total
                    it.copy(
                        loading = false, loadingMore = false, refreshing = false, error = null,
                        posts = newPosts, page = page, hasMore = hasMore
                    )
                }
            }.onFailure { e ->
                _state.update { 
                    it.copy(
                        loading = false, loadingMore = false, refreshing = false, 
                        error = e.message ?: "加载失败"
                    ) 
                }
            }
        }
    }
}
