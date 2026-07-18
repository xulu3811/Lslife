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
) {
    val total: Double get() = entries.sumOf { it.product.price * it.quantity }
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
            repo.upsertCart(entry.product.id, next)
            load()
        }
    }
}
