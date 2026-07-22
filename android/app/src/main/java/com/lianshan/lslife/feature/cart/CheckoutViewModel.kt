package com.lianshan.lslife.feature.cart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Address
import com.lianshan.lslife.core.model.CartEntry
import com.lianshan.lslife.core.network.CreateOrderRequest
import com.lianshan.lslife.core.network.DeliveryAddressBody
import com.lianshan.lslife.core.network.OrderItemRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val entries: List<CartEntry> = emptyList(),
    val address: Address? = null,
    val isCreatingOrder: Boolean = false,
) {
    val itemsTotal: Double get() = entries.sumOf { (it.product?.price ?: it.post?.price ?: 0.0) * it.quantity }
    val deliveryFee: Double get() = if (entries.firstOrNull()?.merchantId != null) (entries.firstOrNull()?.product?.merchant?.deliveryFee ?: 0.0) else 0.0
    val totalAmount: Double get() = itemsTotal + deliveryFee
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val repo: LsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val merchantId = savedStateHandle.get<String>("merchantId")
    private val sellerId = savedStateHandle.get<String>("sellerId")

    private val _state = MutableStateFlow(CheckoutUiState())
    val state: StateFlow<CheckoutUiState> = _state

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            
            // 1. Load addresses
            val addrRes = repo.addresses()
            var defaultAddr: Address? = null
            if (addrRes.isSuccess) {
                val addrs = addrRes.getOrNull() ?: emptyList()
                defaultAddr = addrs.firstOrNull { it.isDefault } ?: addrs.firstOrNull()
            }

            // 2. Load Cart
            repo.cart()
                .onSuccess { allEntries ->
                    val filtered = allEntries.filter { 
                        (merchantId != null && it.merchantId == merchantId) || 
                        (sellerId != null && it.sellerId == sellerId) 
                    }
                    _state.update { it.copy(loading = false, entries = filtered, address = defaultAddr) }
                }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun submitOrder(onSuccess: (String) -> Unit) {
        val st = _state.value
        if (st.address == null) {
            _state.update { it.copy(error = "请先添加收货地址") }
            return
        }
        if (st.entries.isEmpty()) return

        _state.update { it.copy(isCreatingOrder = true, error = null) }
        viewModelScope.launch {
            val req = CreateOrderRequest(
                merchantId = merchantId,
                sellerId = sellerId,
                items = st.entries.map { 
                    OrderItemRequest(
                        productId = it.product?.id, 
                        postId = it.post?.id, 
                        quantity = it.quantity
                    ) 
                },
                deliveryAddress = DeliveryAddressBody(
                    name = st.address.name,
                    phone = st.address.phone,
                    address = st.address.address
                )
            )
            repo.createOrder(req)
                .onSuccess { order ->
                    _state.update { it.copy(isCreatingOrder = false) }
                    onSuccess(order.id)
                }
                .onFailure { e ->
                    _state.update { it.copy(isCreatingOrder = false, error = e.message) }
                }
        }
    }
}
