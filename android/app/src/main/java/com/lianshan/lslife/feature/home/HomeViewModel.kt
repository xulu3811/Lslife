package com.lianshan.lslife.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Merchant
import com.lianshan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 同城信息类目（走 /posts），其余走商家 */
val UGC_CATEGORIES = setOf(
    "second_hand", "job", "house", "housekeeping", "maintenance", "moving", "veggies",
)

data class HomeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val merchants: List<Merchant> = emptyList(),
    val posts: List<Post> = emptyList(),
    val recommended: List<Merchant> = emptyList(),
    val query: String = "",
    val category: String = "all",
    val sort: String = "default",
    val isUgcMode: Boolean = false,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,
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
        _state.update { it.copy(category = c, isUgcMode = c in UGC_CATEGORIES) }
        load()
    }

    fun onSort(s: String) {
        _state.update { it.copy(sort = s) }
        load()
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true) }
        load(showFullLoading = false, page = 1)
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.loadingMore || !s.hasMore) return
        _state.update { it.copy(loadingMore = true) }
        load(showFullLoading = false, page = s.page + 1)
    }

    fun load(showFullLoading: Boolean = true, page: Int = 1) {
        val s = _state.value
        val ugc = s.category in UGC_CATEGORIES
        viewModelScope.launch {
            if (showFullLoading) _state.update { it.copy(loading = true, error = null, isUgcMode = ugc) }
            
            // If loading page 1, we should clear the lists (if full loading) or just overwrite later
            if (page == 1 && showFullLoading) {
                _state.update { it.copy(merchants = emptyList(), posts = emptyList()) }
            }

            if (ugc) {
                repo.posts(category = s.category, mine = false, page = page, pageSize = 20)
                    .onSuccess { resPage ->
                        val list = if (s.query.isBlank()) resPage.list
                        else resPage.list.filter {
                            it.title.contains(s.query, true) || it.description.contains(s.query, true)
                        }
                        _state.update {
                            val newPosts = if (page == 1) list else it.posts + list
                            val hasMore = resPage.page * resPage.pageSize < resPage.total
                            it.copy(
                                loading = false, loadingMore = false, refreshing = false,
                                posts = newPosts, merchants = emptyList(), error = null, isUgcMode = true,
                                page = page, hasMore = hasMore
                            )
                        }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(loading = false, loadingMore = false, refreshing = false, error = e.message ?: "加载失败", isUgcMode = true) }
                    }
                return@launch
            }

            if (page == 1 && s.category in listOf("all", null) && s.query.isBlank()) {
                val cached = repo.getCachedMerchants()
                if (cached.isNotEmpty() && s.merchants.isEmpty()) {
                    _state.update { it.copy(merchants = cached, posts = emptyList(), error = null, isUgcMode = false) }
                }
            }

            repo.merchants(s.category, s.query, s.sort, page = page, pageSize = 20)
                .onSuccess { resPage ->
                    _state.update {
                        val newMerchants = if (page == 1) resPage.list else it.merchants + resPage.list
                        val hasMore = resPage.page * resPage.pageSize < resPage.total
                        it.copy(
                            loading = false, loadingMore = false, refreshing = false,
                            merchants = newMerchants, posts = emptyList(), error = null, isUgcMode = false,
                            page = page, hasMore = hasMore
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { 
                        it.copy(
                            loading = false, loadingMore = false, refreshing = false,
                            error = if (it.merchants.isEmpty()) (e.message ?: "加载失败") else null
                        ) 
                    }
                }
            
            if (page == 1) {
                repo.recommended().onSuccess { rec -> _state.update { it.copy(recommended = rec) } }
            }
        }
    }
}
