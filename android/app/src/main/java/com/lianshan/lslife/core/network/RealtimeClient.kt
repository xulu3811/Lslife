package com.lianshan.lslife.core.network

import com.lianshan.lslife.BuildConfig
import com.lianshan.lslife.core.data.TokenStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebSocket 实时通道客户端。
 * 订阅订单状态与骑手位置的服务端推送 (backend/src/realtime/hub.ts)。
 */
@Singleton
class RealtimeClient @Inject constructor(
    private val client: OkHttpClient,
    private val tokenStore: TokenStore,
) {
    fun events(): Flow<String> = callbackFlow {
        val token = tokenStore.current()
        val request = Request.Builder()
            .url("${BuildConfig.WS_BASE_URL}?token=$token")
            .build()

        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                trySend(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        }

        val ws = client.newWebSocket(request, listener)
        awaitClose { ws.close(1000, "closed") }
    }
}
