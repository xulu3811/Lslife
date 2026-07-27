package com.lianshan.lslife

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.ThemeMode
import com.lianshan.lslife.ui.LsLifeApp
import com.lianshan.lslife.ui.SessionViewModel
import com.lianshan.lslife.ui.theme.LsLifeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val sessionViewModel: SessionViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // 授权结果由系统管理，用户也可在后台设置页再次调整
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        handleChatIntent(intent)
        setContent {
            val themeMode by sessionViewModel.themeMode.collectAsStateWithLifecycle()
            val useDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            LsLifeTheme(darkTheme = useDarkTheme) {
                LsLifeApp(sessionViewModel = sessionViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleChatIntent(intent)
    }

    private fun handleChatIntent(intent: Intent?) {
        val sessionId = intent?.getStringExtra("navigate_to_chat_session_id")
        val senderId = intent?.getStringExtra("navigate_to_chat_sender_id") ?: ""
        val senderName = intent?.getStringExtra("navigate_to_chat_sender_name") ?: "同城买家/商家"
        if (!sessionId.isNullOrBlank()) {
            sessionViewModel.triggerNavigateToChat(sessionId, senderId, senderName)
            intent.removeExtra("navigate_to_chat_session_id")
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                try {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
