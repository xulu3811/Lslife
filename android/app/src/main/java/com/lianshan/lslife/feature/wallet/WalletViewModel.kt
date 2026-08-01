package com.lianshan.lslife.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.WalletTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val isLoading: Boolean = false,
    val balance: Double = 0.0,
    val points: Int = 0,
    val transactions: List<WalletTransaction> = emptyList(),
    val error: String? = null,
    val isRecharging: Boolean = false,
    val rechargeSuccess: Boolean = false
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: LsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadWalletInfo()
    }

    fun loadWalletInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.walletInfo()
                .onSuccess { info ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            balance = info.balance,
                            points = info.points,
                            transactions = info.transactions
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Failed to load wallet info")
                    }
                }
        }
    }

    fun recharge(amount: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRecharging = true, error = null, rechargeSuccess = false) }
            repository.recharge(amount, "cash", "mock")
                .onSuccess { result ->
                    if (result.paid || result.paymentId != null) {
                        // Confirm mock payment if necessary or assume success
                        if (result.orderId != null || result.paymentId != null) {
                            val orderNo = result.paymentId ?: "" // In our mock, paymentId might be used or orderNo returned inside prepayPayload
                            // For simplicity, wait a moment and reload
                            kotlinx.coroutines.delay(1000)
                            loadWalletInfo()
                            _uiState.update { it.copy(isRecharging = false, rechargeSuccess = true) }
                        } else {
                            _uiState.update { it.copy(isRecharging = false, rechargeSuccess = true) }
                        }
                    } else {
                        _uiState.update { it.copy(isRecharging = false, error = "Recharge failed") }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isRecharging = false, error = e.message ?: "Failed to recharge")
                    }
                }
        }
    }

    fun clearRechargeSuccess() {
        _uiState.update { it.copy(rechargeSuccess = false) }
    }
}
