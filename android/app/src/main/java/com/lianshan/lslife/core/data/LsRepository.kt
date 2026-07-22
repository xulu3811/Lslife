package com.lianshan.lslife.core.data

import com.lianshan.lslife.core.database.MerchantDao
import com.lianshan.lslife.core.database.MerchantEntity
import com.lianshan.lslife.core.model.*
import com.lianshan.lslife.core.network.*
import javax.inject.Inject
import javax.inject.Singleton

/** 业务数据仓库: 商家/购物车/订单/支付/发布/会员/通知/AI。 */
@Singleton
class LsRepository @Inject constructor(
    private val api: ApiService,
    private val merchantDao: MerchantDao,
) {
    // 商家 (offline-first: 成功后写入本地缓存, 失败时回退缓存)
    suspend fun merchants(category: String?, q: String?, sort: String, page: Int = 1, pageSize: Int = 20): Result<MerchantPage> {
        val result = safeCall { api.merchants(category = category?.takeIf { it != "all" }, q = q?.takeIf { it.isNotBlank() }, sort = sort, page = page, pageSize = pageSize) }
        result.onSuccess { resPage ->
            if ((category == null || category == "all") && page == 1) {
                runCatching {
                    merchantDao.upsertAll(resPage.list.map { it.toEntity() })
                }
            }
        }
        if (result.isFailure && category in listOf(null, "all") && q.isNullOrBlank() && page == 1) {
            val cached = runCatching { merchantDao.getAll() }.getOrNull().orEmpty()
            if (cached.isNotEmpty()) {
                return Result.success(MerchantPage(cached.size, 1, cached.size, cached.map { it.toMerchant() }))
            }
        }
        return result
    }

    suspend fun getCachedMerchants(): List<Merchant> {
        return runCatching { merchantDao.getAll() }.getOrNull().orEmpty().map { it.toMerchant() }
    }

    suspend fun recommended() = safeCall { api.recommended() }
    suspend fun merchant(id: String) = safeCall { api.merchant(id) }

    // 购物车
    suspend fun cart() = safeCall { api.cart() }
    suspend fun upsertCart(productId: String, quantity: Int) = safeCall { api.upsertCart(CartUpsertRequest(productId, quantity)) }
    suspend fun clearCart(merchantId: String? = null) = safeCall { api.clearCart(merchantId) }

    // 订单
    suspend fun createOrder(req: CreateOrderRequest) = safeCall { api.createOrder(req) }
    suspend fun orders() = safeCall { api.orders() }
    suspend fun order(id: String) = safeCall { api.order(id) }
    suspend fun cancelOrder(id: String) = safeCall { api.cancelOrder(id) }

    // 支付
    suspend fun createPayment(orderId: String, channel: String) = safeCall { api.createPayment(CreatePaymentRequest(orderId, channel)) }
    suspend fun mockConfirm(orderNo: String) = safeCall { api.mockConfirm(MockConfirmRequest(orderNo)) }

    // 地址
    suspend fun addresses() = safeCall { api.addresses() }
    suspend fun addAddress(body: AddressBody) = safeCall { api.addAddress(body) }
    suspend fun updateAddress(id: String, body: AddressBody) = safeCall { api.updateAddress(id, body) }
    suspend fun deleteAddress(id: String) = safeCall { api.deleteAddress(id) }

    // 发布
    suspend fun createPost(req: CreatePostRequest) = safeCall { api.createPost(req) }
    suspend fun posts(
        category: String? = null,
        mine: Boolean? = null,
        q: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ) = safeCall { 
        api.posts(category, mine, q, minPrice, maxPrice, sortBy, page, pageSize) 
    }
    suspend fun quota() = safeCall { api.quota() }
    suspend fun post(id: String) = safeCall { api.post(id) }
    suspend fun updatePost(id: String, req: CreatePostRequest) = safeCall { api.updatePost(id, req) }
    suspend fun updatePostStatus(id: String, status: String) = safeCall { api.updatePostStatus(id, mapOf("status" to status)) }
    suspend fun deletePost(id: String) = safeCall { api.deletePost(id) }

    // 会员
    suspend fun plans() = safeCall { api.plans() }
    suspend fun subscribe(tier: String) = safeCall { api.subscribe(SubscribeRequest(tier)) }

    // 通知
    suspend fun notifications() = safeCall { api.notifications() }
    suspend fun readAllNotifications() = safeCall { api.readAllNotifications() }

    // AI
    suspend fun aiRecommend(prompt: String) = safeCall { api.aiRecommend(AiRequest(prompt)) }
    suspend fun aiGenerateDescription(title: String, category: String, draft: String? = null) =
        safeCall { api.aiGenerateDescription(AiGenerateDescRequest(title, category, draft)) }

    // 上传
    suspend fun uploadImage(part: okhttp3.MultipartBody.Part) = safeCall { api.uploadImage(part) }
}

private fun Merchant.toEntity() = MerchantEntity(
    id = id, name = name, rating = rating, distance = distance, sales = sales, avgPrice = avgPrice,
    tags = tags.joinToString("|"), deliveryFee = deliveryFee, deliveryTime = deliveryTime,
    logo = logo, category = category, cachedAt = System.currentTimeMillis(),
)

private fun MerchantEntity.toMerchant() = Merchant(
    id = id, name = name, rating = rating, distance = distance, sales = sales, avgPrice = avgPrice,
    tags = if (tags.isBlank()) emptyList() else tags.split("|"), deliveryFee = deliveryFee, deliveryTime = deliveryTime,
    logo = logo, banner = logo, isFood = false, category = category, latitude = 0.0, longitude = 0.0,
    description = "", address = "", phone = "", items = emptyList(),
)
