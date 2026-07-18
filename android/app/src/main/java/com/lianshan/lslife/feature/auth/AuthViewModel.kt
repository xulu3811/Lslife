package com.lianshan.lslife.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val phone: String = "",
    val code: String = "",
    val codeSent: Boolean = false,
    val loading: Boolean = false,
    val message: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v.filter(Char::isDigit).take(11)) }
    fun onCodeChange(v: String) = _state.update { it.copy(code = v.filter(Char::isDigit).take(6)) }
    fun clearMessage() = _state.update { it.copy(message = null) }

    fun sendCode() {
        val phone = _state.value.phone
        if (!phone.matches(Regex("^1\\d{10}$"))) {
            _state.update { it.copy(message = "请输入正确的11位手机号") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            authRepository.sendCode(phone)
                .onSuccess { r ->
                    // mock 环境自动回填验证码, 方便体验
                    _state.update { it.copy(loading = false, codeSent = true, code = r.mockCode ?: it.code, message = "验证码已发送") }
                }
                .onFailure { e -> _state.update { it.copy(loading = false, message = e.message ?: "发送失败") } }
        }
    }

    fun login() {
        val s = _state.value
        if (s.code.length != 6) {
            _state.update { it.copy(message = "请输入6位验证码") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            authRepository.login(s.phone, s.code)
                .onSuccess { _state.update { it.copy(loading = false, success = true) } }
                .onFailure { e -> _state.update { it.copy(loading = false, message = e.message ?: "登录失败") } }
        }
    }
}
