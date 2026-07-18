package com.lianshan.lslife.feature.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Merchant
import com.lianshan.lslife.core.network.CreateOrderRequest
import com.lianshan.lslife.core.network.DeliveryAddressBody
import com.lianshan.lslife.core.network.OrderItemRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MerchantUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val merchant: Merchant? = null,
    val quantities: Map<String, Int> = emptyMap(),
    val checkingOut: Boolean = false,
    val message: String? = null,
    val checkedOutOrderId: String? = null,
) {
    val itemsTotal: Double
        get() = merchant?.items.orEmpty().sumOf { (quantities[it.id] ?: 0) * it.price }
    val totalCount: Int get() = quantities.values.sum()
    val payable: Double get() = if (totalCount > 0) itemsTotal + (merchant?.deliveryFee ?: 0.0) else 0.0
}

@HiltViewModel
class MerchantDetailViewModel @Inject constructor(
    private val repo: LsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MerchantUiState())
    val state: StateFlow<MerchantUiState> = _state

    fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            repo.merchant(id)
                .onSuccess { m -> _state.update { it.copy(loading = false, merchant = m) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun changeQty(productId: String, delta: Int) {
        _state.update {
            val next = ((it.quantities[productId] ?: 0) + delta).coerceAtLeast(0)
            val map = it.quantities.toMutableMap()
            if (next == 0) map.remove(productId) else map[productId] = next
            it.copy(quantities = map)
        }
        // 同步到服务端购物车
        val qty = _state.value.quantities[productId] ?: 0
        viewModelScope.launch { repo.upsertCart(productId, qty) }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }

    /** 结算: 下单 -> 创建支付(mock) -> 确认支付 -> 回传订单号 */
    fun checkout() {
        val s = _state.value
        val m = s.merchant ?: return
        if (s.totalCount == 0) {
            _state.update { it.copy(message = "请先选择商品") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(checkingOut = true) }
            // 取默认地址
            val address = repo.addresses().getOrNull()?.firstOrNull { it.isDefault }
                ?: repo.addresses().getOrNull()?.firstOrNull()
            if (address == null) {
                _state.update { it.copy(checkingOut = false, message = "请先在“我的”添加收货地址") }
                return@launch
            }

            val items = s.quantities.map { OrderItemRequest(it.key, it.value) }
            val orderResult = repo.createOrder(
                CreateOrderRequest(
                    merchantId = m.id,
                    items = items,
                    deliveryAddress = DeliveryAddressBody(address.name, address.phone, address.address),
                ),
            )
            val order = orderResult.getOrElse {
                _state.update { st -> st.copy(checkingOut = false, message = it.message ?: "下单失败") }
                return@launch
            }

            repo.createPayment(order.id, "mock")
            val pay = repo.mockConfirm(order.orderNo)
            pay.onSuccess {
                _state.update { st -> st.copy(checkingOut = false, checkedOutOrderId = order.id, quantities = emptyMap()) }
            }.onFailure {
                _state.update { st -> st.copy(checkingOut = false, message = it.message ?: "支付失败") }
            }
        }
    }
}
