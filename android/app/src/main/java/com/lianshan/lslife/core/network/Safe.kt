package com.lianshan.lslife.core.network

import com.lianshan.lslife.core.model.ApiEnvelope

class ApiException(val code: Int, message: String) : Exception(message)

/** 解包 ApiEnvelope, 业务码非 0 抛 ApiException, 网络异常向上抛出。 */
suspend fun <T> safeCall(block: suspend () -> ApiEnvelope<T>): Result<T> = runCatching {
    val env = block()
    if (env.code != 0 || env.data == null) throw ApiException(env.code, env.message)
    env.data
}
