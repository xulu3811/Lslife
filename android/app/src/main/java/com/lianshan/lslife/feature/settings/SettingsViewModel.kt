package com.lianshan.lslife.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.TokenStore
import com.lianshan.lslife.core.database.MerchantDao
import com.lianshan.lslife.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val clearingCache: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenStore: TokenStore,
    private val merchantDao: MerchantDao,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    init {
        viewModelScope.launch {
            combine(
                tokenStore.themeModeFlow,
                tokenStore.notificationsEnabledFlow,
            ) { themeMode, notificationsEnabled ->
                themeMode to notificationsEnabled
            }.collect { (themeMode, notificationsEnabled) ->
                _state.update {
                    it.copy(
                        themeMode = themeMode,
                        notificationsEnabled = notificationsEnabled,
                    )
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { tokenStore.setThemeMode(mode) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            tokenStore.setNotificationsEnabled(enabled)
            _state.update {
                it.copy(message = if (enabled) "消息通知已开启" else "消息通知已关闭")
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _state.update { it.copy(clearingCache = true) }
            runCatching { merchantDao.clear() }
                .onSuccess {
                    _state.update { it.copy(clearingCache = false, message = "本地缓存已清理") }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            clearingCache = false,
                            message = error.message ?: "缓存清理失败",
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
