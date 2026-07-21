package com.lianshan.lslife.core.data

import com.lianshan.lslife.core.model.LoginResult
import com.lianshan.lslife.core.model.User
import com.lianshan.lslife.core.network.ApiService
import com.lianshan.lslife.core.network.LoginRequest
import com.lianshan.lslife.core.network.RealNameRequest
import com.lianshan.lslife.core.network.RegisterRequest
import com.lianshan.lslife.core.network.safeCall
import com.lianshan.lslife.core.network.ResetPasswordRequest
import com.lianshan.lslife.core.network.SendEmailCodeRequest
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

    suspend fun register(phone: String, email: String, password: String, nickname: String? = null): Result<LoginResult> =
        safeCall { api.register(RegisterRequest(phone, email, password, nickname)) }
            .onSuccess { tokenStore.save(it.token) }

    suspend fun sendEmailCode(email: String): Result<Map<String, String>> =
        safeCall { api.sendEmailCode(SendEmailCodeRequest(email)) }

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Map<String, String>> =
        safeCall { api.resetPassword(ResetPasswordRequest(email, code, newPassword)) }

    suspend fun login(phone: String, password: String): Result<LoginResult> =
        safeCall { api.login(LoginRequest(phone, password)) }
            .onSuccess { tokenStore.save(it.token) }

    suspend fun me(): Result<User> = safeCall { api.me() }

    suspend fun realName(name: String, idCard: String): Result<User> =
        safeCall { api.realName(RealNameRequest(name, idCard)) }

    suspend fun updateProfile(nickname: String?, avatar: String?): Result<User> =
        safeCall { api.updateProfile(com.lianshan.lslife.core.model.ProfileUpdateRequest(nickname, avatar)) }

    suspend fun logout() = tokenStore.clear()
}
