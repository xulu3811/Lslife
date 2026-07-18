package com.lianshan.lslife.core.data

import com.lianshan.lslife.core.model.LoginResult
import com.lianshan.lslife.core.model.SendCodeResult
import com.lianshan.lslife.core.model.User
import com.lianshan.lslife.core.network.ApiService
import com.lianshan.lslife.core.network.LoginRequest
import com.lianshan.lslife.core.network.RealNameRequest
import com.lianshan.lslife.core.network.SendCodeRequest
import com.lianshan.lslife.core.network.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) {
    val isLoggedIn: Flow<Boolean> = tokenStore.tokenFlow.map { it != null }

    suspend fun sendCode(phone: String): Result<SendCodeResult> =
        safeCall { api.sendCode(SendCodeRequest(phone)) }

    suspend fun login(phone: String, code: String): Result<LoginResult> =
        safeCall { api.login(LoginRequest(phone, code)) }.onSuccess { tokenStore.save(it.token) }

    suspend fun me(): Result<User> = safeCall { api.me() }

    suspend fun realName(name: String, idCard: String): Result<User> =
        safeCall { api.realName(RealNameRequest(name, idCard)) }

    suspend fun logout() = tokenStore.clear()
}
