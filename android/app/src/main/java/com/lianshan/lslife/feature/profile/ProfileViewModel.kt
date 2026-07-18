package com.lianshan.lslife.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.AuthRepository
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.model.MembershipPlan
import com.lianshan.lslife.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val plans: List<MembershipPlan> = emptyList(),
    val unread: Int = 0,
    val message: String? = null,
    val loggedOut: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val repo: LsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            authRepository.me().onSuccess { u -> _state.update { it.copy(loading = false, user = u) } }
            repo.plans().onSuccess { p -> _state.update { it.copy(plans = p) } }
            repo.notifications().onSuccess { n -> _state.update { it.copy(unread = n.unread) } }
        }
    }

    fun subscribe(tier: String) {
        viewModelScope.launch {
            repo.subscribe(tier)
                .onSuccess { _state.update { it.copy(message = "会员开通成功") }; load() }
                .onFailure { e -> _state.update { it.copy(message = e.message ?: "开通失败") } }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.update { it.copy(loggedOut = true) }
        }
    }
}
