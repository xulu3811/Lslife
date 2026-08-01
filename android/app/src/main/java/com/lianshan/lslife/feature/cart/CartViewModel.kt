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

    fun toggleEntrySelection(entry: CartEntry) {
        val groupId = entry.merchantId ?: entry.sellerId ?: "unknown"
        _state.update { st ->
            val currentSelected = st.entries.filter { it.id in st.selectedEntryIds }
            val currentGroupId = currentSelected.firstOrNull()?.let { it.merchantId ?: it.sellerId ?: "unknown" }
            
            val newSelected = if (currentGroupId != null && currentGroupId != groupId) {
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
                st.copy(selectedEntryIds = emptySet())
            } else {
                // Select all in this group, clearing any other group
                st.copy(selectedEntryIds = groupEntryIds)
            }
        }
    }

    fun setDeliveryMethod(method: String) {
        _state.update { it.copy(deliveryMethod = method) }
    }
}
