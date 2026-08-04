package com.lianshan.lslife.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.CartEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val entries: List<CartEntry> = emptyList(),
    val selectedEntryIds: Set<String> = emptySet(),
    val deliveryMethod: String = "DELIVERY", // DELIVERY | PICKUP
    val isManaging: Boolean = false,
) {
    val total: Double get() = entries
        .filter { it.id in selectedEntryIds }
        .sumOf { (it.product?.price ?: it.post?.price ?: 0.0) * it.quantity }
        
    val savedAmount: Double get() = entries
        .filter { it.id in selectedEntryIds }
        .sumOf { 
            val price = it.product?.price ?: it.post?.price ?: 0.0
            val original = it.product?.originalPrice
            if (original != null && original > price) {
                (original - price) * it.quantity
            } else 0.0
        }
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repo: LsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CartUiState())
    val state: StateFlow<CartUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            repo.cart()
                .onSuccess { list -> _state.update { it.copy(loading = false, entries = list) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun changeQty(entry: CartEntry, delta: Int) {
        val next = (entry.quantity + delta).coerceAtLeast(0)
        viewModelScope.launch {
            repo.upsertCart(
                productId = entry.product?.id,
                postId = entry.post?.id,
                quantity = next
            )
            load()
        }
    }

    fun toggleManageMode() {
        _state.update { it.copy(isManaging = !it.isManaging, selectedEntryIds = emptySet()) }
    }

    fun removeSelectedItems() {
        viewModelScope.launch {
            val selectedIds = _state.value.selectedEntryIds
            val entriesToRemove = _state.value.entries.filter { it.id in selectedIds }
            
            // 乐观更新
            _state.update { st ->
                st.copy(
                    entries = st.entries.filter { it.id !in selectedIds },
                    selectedEntryIds = emptySet(),
                    isManaging = false
                )
            }
            
            // 网络请求
            entriesToRemove.forEach { entry ->
                repo.upsertCart(
                    productId = entry.product?.id,
                    postId = entry.post?.id,
                    quantity = 0
                )
            }
            load()
        }
    }

    fun toggleEntrySelection(entry: CartEntry) {
        val groupId = entry.merchantId ?: entry.sellerId ?: "unknown"
        _state.update { st ->
            val currentSelected = st.entries.filter { it.id in st.selectedEntryIds }
            val currentGroupId = currentSelected.firstOrNull()?.let { it.merchantId ?: it.sellerId ?: "unknown" }
            
            val newSelected = if (!st.isManaging && currentGroupId != null && currentGroupId != groupId) {
                // User selected an item from a different merchant. Clear previous selection to enforce single-merchant checkout.
                setOf(entry.id)
            } else {
                if (st.selectedEntryIds.contains(entry.id)) {
                    st.selectedEntryIds - entry.id
                } else {
                    st.selectedEntryIds + entry.id
                }
            }
            st.copy(selectedEntryIds = newSelected)
        }
    }

    fun toggleGroupSelection(groupId: String) {
        _state.update { st ->
            val groupEntries = st.entries.filter { (it.merchantId ?: it.sellerId ?: "unknown") == groupId }
            val groupEntryIds = groupEntries.map { it.id }.toSet()
            
            val allSelected = st.selectedEntryIds.containsAll(groupEntryIds) && groupEntryIds.isNotEmpty()
            if (allSelected) {
                st.copy(selectedEntryIds = st.selectedEntryIds - groupEntryIds)
            } else {
                if (st.isManaging) {
                    st.copy(selectedEntryIds = st.selectedEntryIds + groupEntryIds)
                } else {
                    // Select all in this group, clearing any other group
                    st.copy(selectedEntryIds = groupEntryIds)
                }
            }
        }
    }

    fun selectAll() {
        _state.update { st ->
            if (st.isManaging) {
                val allIds = st.entries.map { it.id }.toSet()
                val isAllSelected = st.selectedEntryIds.containsAll(allIds) && allIds.isNotEmpty()
                st.copy(selectedEntryIds = if (isAllSelected) emptySet() else allIds)
            } else {
                // In normal mode, select all selects the first group or active group
                val activeGroupId = st.entries.firstOrNull { it.id in st.selectedEntryIds }?.let { it.merchantId ?: it.sellerId ?: "unknown" } 
                    ?: st.entries.firstOrNull()?.let { it.merchantId ?: it.sellerId ?: "unknown" }
                
                if (activeGroupId != null) {
                    val groupEntryIds = st.entries.filter { (it.merchantId ?: it.sellerId ?: "unknown") == activeGroupId }.map { it.id }.toSet()
                    val allSelected = st.selectedEntryIds.containsAll(groupEntryIds) && groupEntryIds.isNotEmpty()
                    st.copy(selectedEntryIds = if (allSelected) emptySet() else groupEntryIds)
                } else {
                    st.copy()
                }
            }
        }
    }

    fun setDeliveryMethod(method: String) {
        _state.update { it.copy(deliveryMethod = method) }
    }
}
