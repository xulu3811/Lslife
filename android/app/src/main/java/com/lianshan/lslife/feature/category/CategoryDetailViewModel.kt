package com.lianshan.lslife.feature.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.CategoryRepository
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.CategoryNode
import com.lianshan.lslife.core.model.CategorySchemaResponse
import com.lianshan.lslife.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryDetailUiState(
    val parentCategoryId: String = "",
    val parentCategoryName: String = "",
    val loading: Boolean = true,
    val error: String? = null,
    val categoryTree: List<CategoryNode> = emptyList(),
    val subCategories: List<CategoryNode> = emptyList(),
    val leafCategories: List<CategoryNode> = emptyList(),
    val posts: List<Post> = emptyList(),
    val selectedSubCategory: String = "all",
    val selectedLeafCategory: String = "all",
    val sort: String = "default",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val publisherType: String? = null,
    val listingType: String? = null,
    val attributesFilter: Map<String, Set<String>> = emptyMap(),
    val currentSchema: CategorySchemaResponse? = null,
    val showFilterBottomSheet: Boolean = false,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,
)

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: LsRepository,
    private val categoryRepo: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryDetailUiState())
    val state: StateFlow<CategoryDetailUiState> = _state

    init {
        val catId = savedStateHandle.get<String>("categoryId") ?: ""
        val catName = savedStateHandle.get<String>("categoryName") ?: ""
        _state.update { it.copy(parentCategoryId = catId, parentCategoryName = catName) }

        observeCategoryTree()
        load()
    }

    private fun observeCategoryTree() {
        viewModelScope.launch {
            categoryRepo.getCategoryTree()
        }
        viewModelScope.launch {
            categoryRepo.categoryTree.collect { tree ->
                val parentId = _state.value.parentCategoryId
                val parentNode = tree.find { it.id == parentId }
                val subs = parentNode?.children ?: emptyList()
                _state.update { it.copy(categoryTree = tree, subCategories = subs) }
            }
        }
    }

    fun onSubCategory(c: String) {
        val newCat = if (_state.value.selectedSubCategory == c && c != "all") "all" else c
        
        val tree = _state.value.categoryTree
        val parentId = _state.value.parentCategoryId
        val parentNode = tree.find { it.id == parentId }
        val targetNode = parentNode?.children?.find { it.id == newCat }
        val leafCats = targetNode?.children ?: emptyList()

        _state.update { it.copy(
            selectedSubCategory = newCat,
            leafCategories = leafCats,
            selectedLeafCategory = "all"
        ) }
        
        val effectiveCat = if (newCat == "all") _state.value.parentCategoryId else newCat
        
        viewModelScope.launch {
            if (effectiveCat == "all") {
                _state.update { it.copy(currentSchema = null, attributesFilter = emptyMap()) }
            } else {
                repo.categorySchema(effectiveCat).onSuccess { schema ->
                    _state.update { it.copy(currentSchema = schema, attributesFilter = emptyMap()) }
                }
            }
            load(showFullLoading = true, page = 1)
        }
    }

    fun onLeafCategory(c: String) {
        val newCat = if (_state.value.selectedLeafCategory == c && c != "all") "all" else c
        _state.update { it.copy(selectedLeafCategory = newCat) }

        val effectiveCat = if (newCat == "all") {
            if (_state.value.selectedSubCategory == "all") _state.value.parentCategoryId else _state.value.selectedSubCategory
        } else {
            newCat
        }

        viewModelScope.launch {
            if (effectiveCat == "all") {
                _state.update { it.copy(currentSchema = null, attributesFilter = emptyMap()) }
            } else {
                repo.categorySchema(effectiveCat).onSuccess { schema ->
                    _state.update { it.copy(currentSchema = schema, attributesFilter = emptyMap()) }
                }
            }
            load(showFullLoading = true, page = 1)
        }
    }

    fun onSort(s: String) {
        _state.update { it.copy(sort = s) }
        load()
    }

    fun updatePrice(min: Double?, max: Double?) {
        _state.update { it.copy(minPrice = min, maxPrice = max) }
        load()
    }

    fun updateAttributeFilter(key: String, value: String) {
        val current = _state.value.attributesFilter.toMutableMap()
        val currentSet = (current[key] ?: emptySet()).toMutableSet()
        if (currentSet.contains(value)) {
            currentSet.remove(value)
            if (currentSet.isEmpty()) current.remove(key) else current[key] = currentSet
        } else {
            currentSet.add(value)
            current[key] = currentSet
        }
        _state.update { it.copy(attributesFilter = current) }
        load()
    }

    fun updatePublisherType(type: String?) {
        _state.update { it.copy(publisherType = type) }
        load()
    }

    fun updateListingType(type: String?) {
        _state.update { it.copy(listingType = type) }
        load()
    }

    fun clearAttributesFilter() {
        _state.update { it.copy(attributesFilter = emptyMap(), minPrice = null, maxPrice = null, publisherType = null, listingType = null) }
        load()
    }

    fun setShowFilterBottomSheet(show: Boolean) {
        _state.update { it.copy(showFilterBottomSheet = show) }
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
        viewModelScope.launch {
            if (showFullLoading) _state.update { it.copy(loading = true, error = null) }
            
            if (page == 1 && showFullLoading) {
                _state.update { it.copy(posts = emptyList()) }
            }

            val catParam = if (s.selectedLeafCategory != "all" && s.selectedLeafCategory.isNotBlank()) {
                s.selectedLeafCategory
            } else if (s.selectedSubCategory != "all" && s.selectedSubCategory.isNotBlank()) {
                s.selectedSubCategory
            } else {
                s.parentCategoryId
            }
            val sortParam = if (s.sort == "default") null else s.sort

            repo.posts(
                category = catParam.takeIf { it.isNotBlank() }, 
                publisherType = s.publisherType,
                listingType = s.listingType,
                mine = false, 
                q = null, 
                sortBy = sortParam, 
                minPrice = s.minPrice,
                maxPrice = s.maxPrice,
                attrFilter = s.attributesFilter.takeIf { it.isNotEmpty() }?.mapValues { it.value.joinToString("||") },
                page = page, 
                pageSize = 20
            )
                .onSuccess { resPage ->
                    val list = resPage.list
                    _state.update {
                        val newPosts = if (page == 1) list else it.posts + list
                        val hasMore = resPage.page * resPage.pageSize < resPage.total
                        it.copy(
                            loading = false, loadingMore = false, refreshing = false,
                            posts = newPosts, error = null,
                            page = page, hasMore = hasMore
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, loadingMore = false, refreshing = false, error = e.message ?: "加载失败") }
                }
        }
    }

    fun addToCart(postId: String) {
        viewModelScope.launch {
            repo.upsertCart(postId = postId, quantity = 1)
        }
    }
}
