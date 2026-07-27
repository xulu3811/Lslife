package com.lianshan.lslife.core.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.lianshan.lslife.LsLifeApplication
import com.lianshan.lslife.MainActivity
import com.lianshan.lslife.R
import com.lianshan.lslife.core.data.AuthRepository
import com.lianshan.lslife.core.data.ChatRepository
import com.lianshan.lslife.core.network.RealtimeClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 即时通信后台保活守护服务。
 * 1. 挂载持续前台通知，大幅提升系统进程优先级（防杀）。
 * 2. 独占托管实时 WebSocket 监听，并在收到离线或后台消息时发射手机顶栏横幅弹窗（Heads-up Notification）。
 * 3. 监听网络变动广播，在 4G/5G/Wi-Fi 切换时触发 WebSocket 离线同步与重连对齐。
 */
@AndroidEntryPoint
class LsLifeImService : Service() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var chatRepository: ChatRepository
    @Inject lateinit var realtimeClient: RealtimeClient

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var lastNotificationTime = 0L
    private val notificationDebounceMs = 1500L

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        observeIncomingMessages()
        registerNetworkListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceNotification()
        serviceScope.launch {
            try {
                val sessions = chatRepository.getSessions()
                sessions.forEach { s ->
                    try { realtimeClient.sendOfflineSync(s.id) } catch (e: Exception) {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundServiceNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, LsLifeApplication.CHANNEL_ID_FOREGROUND)
            .setContentTitle("连山同城生活：即时通信守护中")
            .setContentText("保持交易沟通长连接，保护对话隐私与订单通知不漏回")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeIncomingMessages() {
        serviceScope.launch {
            chatRepository.incomingMessages().collect { msg ->
                val myId = authRepository.cachedMe()?.id
                if (msg.senderId != myId && !msg.isRecalled) {
                    chatRepository.syncLocalMessageQuietly(msg)

                    val isForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                    if (!isForeground && !msg.isOfflineSync) {
                        val now = System.currentTimeMillis()
                        if (now - lastNotificationTime > notificationDebounceMs) {
                            lastNotificationTime = now
                            showTopBarPopupNotification(
                                sessionId = msg.sessionId,
                                senderId = msg.senderId,
                                content = if (msg.type == "image") "[图片]" else msg.content
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showTopBarPopupNotification(sessionId: String, senderId: String, content: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_chat_session_id", sessionId)
            putExtra("navigate_to_chat_sender_id", senderId)
            putExtra("navigate_to_chat_sender_name", "同城买家/商家")
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, LsLifeApplication.CHANNEL_ID_IM)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("收到新消息")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        try {
            NotificationManagerCompat.from(this).notify(sessionId.hashCode(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerNetworkListener() {
        try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    serviceScope.launch {
                        try {
                            val sessions = chatRepository.getSessions()
                            sessions.forEach { s -> realtimeClient.sendOfflineSync(s.id) }
                        } catch (e: Exception) {}
                    }
                }
            }
            cm.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            networkCallback?.let {
                val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                cm.unregisterNetworkCallback(it)
            }
        } catch (e: Exception) {}
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 10086

        fun start(context: Context) {
            try {
                val intent = Intent(context, LsLifeImService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, LsLifeImService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
