package com.lianshan.lslife.ui

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.AuthRepository
import com.lianshan.lslife.core.data.ChatRepository
import com.lianshan.lslife.core.data.TokenStore
import com.lianshan.lslife.core.model.NotificationMode
import com.lianshan.lslife.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val chatRepository: ChatRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val isLoggedIn = authRepository.isLoggedIn.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null,
    )

    val themeMode = tokenStore.themeModeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ThemeMode.SYSTEM,
    )

    val unreadCount: StateFlow<Int> = chatRepository.unreadCount

    init {
        viewModelScope.launch {
            authRepository.isLoggedIn.collect { loggedIn ->
                if (loggedIn == true) {
                    refreshUnreadCount()
                }
            }
        }

        viewModelScope.launch {
            chatRepository.incomingMessages().collect { msg ->
                val myId = authRepository.cachedMe()?.id
                if (msg.senderId != myId) {
                    chatRepository.incrementUnread()
                    val mode = tokenStore.notificationModeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, NotificationMode.RINGTONE).value
                    playNotificationAlert(mode)
                }
            }
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            try {
                chatRepository.getSessions() // This automatically updates unreadCount inside ChatRepository
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun playNotificationAlert(mode: NotificationMode) {
        when (mode) {
            NotificationMode.RINGTONE -> {
                try {
                    val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(context, notification)
                    r.play()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            NotificationMode.VIBRATE -> {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(300)
                }
            }
            NotificationMode.SILENT -> {}
        }
    }
}
