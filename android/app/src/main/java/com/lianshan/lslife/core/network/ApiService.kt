package com.lianshan.lslife.core.network

import com.lianshan.lslife.core.model.*
import retrofit2.http.*

/** 后端 REST 接口, 对应 backend/src/modules。统一响应包裹在 ApiEnvelope。 */
interface ApiService {

    @GET("health")
    suspend fun health(): ApiEnvelope<Map<String, String>>

    // 鉴权
    @POST("auth/send-code")
    suspend fun sendCode(@Body body: SendCodeRequest): ApiEnvelope<SendCodeResult>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiEnvelope<LoginResult>

    @GET("auth/me")
    suspend fun me(): ApiEnvelope<User>

    @POST("auth/realname")
    suspend fun realName(@Body body: RealNameRequest): ApiEnvelope<User>

    // 商家
    @GET("merchants")
    suspend fun merchants(
        @Query("category") category: String? = null,
        @Query("q") q: String? = null,
        @Query("sort") sort: String = "default",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiEnvelope<MerchantPage>

    @GET("merchants/recommended")
    suspend fun recommended(): ApiEnvelope<List<Merchant>>

    @GET("merchants/{id}")
    suspend fun merchant(@Path("id") id: String): ApiEnvelope<Merchant>

    // 购物车
    @GET("cart")
    suspend fun cart(): ApiEnvelope<List<CartEntry>>

    @POST("cart")
    suspend fun upsertCart(@Body body: CartUpsertRequest): ApiEnvelope<Map<String, kotlinx.serialization.json.JsonElement>>

    @DELETE("cart")
    suspend fun clearCart(@Query("merchantId") merchantId: String? = null): ApiEnvelope<Map<String, Boolean>>

    // 订单
    @POST("orders")
    suspend fun createOrder(@Body body: CreateOrderRequest): ApiEnvelope<Order>

    @GET("orders")
    suspend fun orders(): ApiEnvelope<List<Order>>

    @GET("orders/{id}")
    suspend fun order(@Path("id") id: String): ApiEnvelope<Order>

    @POST("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): ApiEnvelope<Order>

    // 支付
    @POST("payments/create")
    suspend fun createPayment(@Body body: CreatePaymentRequest): ApiEnvelope<PaymentCreateResult>

    @POST("payments/mock-confirm")
    suspend fun mockConfirm(@Body body: MockConfirmRequest): ApiEnvelope<PaymentCreateResult>

    // 收货地址
    @GET("addresses")
    suspend fun addresses(): ApiEnvelope<List<Address>>

    @POST("addresses")
    suspend fun addAddress(@Body body: AddressBody): ApiEnvelope<Address>

    @PUT("addresses/{id}")
    suspend fun updateAddress(@Path("id") id: String, @Body body: AddressBody): ApiEnvelope<Address>

    @DELETE("addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): ApiEnvelope<Map<String, Boolean>>

    // 同城发布
    @POST("posts")
    suspend fun createPost(@Body body: CreatePostRequest): ApiEnvelope<Post>

    @GET("posts")
    suspend fun posts(@Query("category") category: String? = null, @Query("mine") mine: Boolean? = null): ApiEnvelope<List<Post>>

    @GET("posts/quota")
    suspend fun quota(): ApiEnvelope<Quota>

    // 会员
    @GET("membership/plans")
    suspend fun plans(): ApiEnvelope<List<MembershipPlan>>

    @POST("membership/subscribe")
    suspend fun subscribe(@Body body: SubscribeRequest): ApiEnvelope<Map<String, kotlinx.serialization.json.JsonElement>>

    // 通知
    @GET("notifications")
    suspend fun notifications(): ApiEnvelope<NotificationResult>

    @POST("notifications/read-all")
    suspend fun readAllNotifications(): ApiEnvelope<Map<String, Boolean>>

    // AI 助手
    @POST("ai/recommend")
    suspend fun aiRecommend(@Body body: AiRequest): ApiEnvelope<AiReply>
}
