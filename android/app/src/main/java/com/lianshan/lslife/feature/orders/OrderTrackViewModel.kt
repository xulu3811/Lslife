package com.lianshan.lslife.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.Order
import com.lianshan.lslife.core.network.RealtimeClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class TrackUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val order: Order? = null,
)

@HiltViewModel
class OrderTrackViewModel @Inject constructor(
    private val repo: LsRepository,
    private val realtimeClient: RealtimeClient,
) : ViewModel() {

    private val _state = MutableStateFlow(TrackUiState())
    val state: StateFlow<TrackUiState> = _state

    /**
     * 生产环境改用 WebSocket (RealtimeClient) 接收骑手真实 GPS 推送, 降低延迟与流量。
     */
    fun start(orderId: String) {
        viewModelScope.launch {
            repo.order(orderId)
                .onSuccess { o -> _state.update { it.copy(loading = false, order = o, error = null) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        }

        viewModelScope.launch {
            realtimeClient.events().collect { text ->
                try {
                    val json = JSONObject(text)
                    if (json.optString("orderId") != orderId) return@collect
                    
                    val event = json.optString("event")
                    if (event == "rider_location") {
                        val lat = json.optDouble("lat")
                        val lng = json.optDouble("lng")
                        val status = json.optString("status")
                        
                        _state.update { st ->
                            val currentOrder = st.order ?: return@update st
                            val currentDelivery = currentOrder.delivery ?: return@update st
                            val currentRider = currentDelivery.rider
                            
                            st.copy(
                                order = currentOrder.copy(
                                    status = status.ifEmpty { currentOrder.status },
                                    delivery = currentDelivery.copy(
                                        status = status.ifEmpty { currentDelivery.status },
                                        rider = currentRider.copy(lat = lat, lng = lng)
                                    )
                                )
                            )
                        }
                    } else if (event == "order_delivered") {
                        _state.update { st ->
                            val currentOrder = st.order ?: return@update st
                            val currentDelivery = currentOrder.delivery ?: return@update st
                            st.copy(
                                order = currentOrder.copy(
                                    status = "delivered",
                                    delivery = currentDelivery.copy(
                                        status = "delivered",
                                        progress = 100
                                    )
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
