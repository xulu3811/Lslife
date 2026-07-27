package com.lianshan.lslife

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LsLifeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // 1. 即时通信高优横幅弹窗通知渠道 (必须为 IMPORTANCE_HIGH 才能弹窗横幅)
            val imChannel = NotificationChannel(
                CHANNEL_ID_IM,
                "即时通信新消息提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "接收同城买家、商家及业务订单的实时聊天弹窗与横幅提示"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            
            // 2. 前台保活守护服务长驻通知渠道
            val fgChannel = NotificationChannel(
                CHANNEL_ID_FOREGROUND,
                "即时通信后台守护服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持即时通信长连接在应用挂起或后台时的持续运作，防止漏回商机"
                enableVibration(false)
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannels(listOf(imChannel, fgChannel))
        }
    }

    companion object {
        const val CHANNEL_ID_IM = "lslife_im_channel"
        const val CHANNEL_ID_FOREGROUND = "lslife_foreground_service"
    }
}
