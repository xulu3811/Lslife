package com.lianshan.lslife.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
)

@Serializable
data class User(
    val id: String,
    val phone: String,
    val nickname: String,
    val avatar: String? = null,
    val membershipTier: String = "free",
    val realNameStatus: String = "none",
    val walletBalance: Double = 0.0,
    val points: Int = 0,
)

@Serializable
data class LoginResult(val token: String, val user: User)

@Serializable
data class SendCodeResult(val sent: Boolean, val mockCode: String? = null)

@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val originalPrice: Double? = null,
    val desc: String,
    val sales: Int = 0,
    val image: String,
    val category: String,
)

@Serializable
data class Merchant(
    val id: String,
    val name: String,
    val rating: Double,
    val distance: Double,
    val sales: Int,
    val avgPrice: Double,
    val tags: List<String> = emptyList(),
    val deliveryFee: Double,
    val deliveryTime: Int,
    val logo: String,
    val banner: String,
    val isFood: Boolean = false,
    val category: String? = null,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val address: String,
    val phone: String,
    val items: List<Product> = emptyList(),
)

@Serializable
data class MerchantPage(
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val list: List<Merchant>,
)

@Serializable
data class CartEntry(
    val id: String,
    val quantity: Int,
    val merchantId: String,
    val product: Product,
)

@Serializable
data class OrderItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val image: String,
)

@Serializable
data class Rider(
    val name: String,
    val phone: String,
    val avatar: String,
    val lat: Double,
    val lng: Double,
)

@Serializable
data class Delivery(
    val status: String,
    val progress: Int,
    val secondsRemaining: Int,
    val rider: Rider,
)

@Serializable
data class Order(
    val id: String,
    val orderNo: String,
    val merchantId: String,
    val merchantName: String,
    val merchantLogo: String,
    val itemsTotal: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val status: String,
    val deliveryName: String,
    val deliveryPhone: String,
    val deliveryAddress: String,
    val createdAt: String,
    val items: List<OrderItem> = emptyList(),
    val delivery: Delivery? = null,
)

@Serializable
data class PaymentCreateResult(
    val paymentId: String? = null,
    val prepayPayload: Map<String, kotlinx.serialization.json.JsonElement>? = null,
    val paid: Boolean = false,
    val orderId: String? = null,
)

@Serializable
data class Address(
    val id: String,
    val name: String,
    val phone: String,
    val tag: String,
    val address: String,
    val isDefault: Boolean,
)

@Serializable
data class Post(
    val id: String,
    val category: String,
    val title: String,
    val description: String,
    val price: Double? = null,
    val images: List<String> = emptyList(),
    val status: String,
    val createdAt: String,
)

@Serializable
data class Quota(val used: Int, val limit: Int, val tier: String)

@Serializable
data class MembershipPlan(
    val tier: String,
    val name: String,
    val price: Double,
    val period: String,
    val benefits: List<String>,
)

@Serializable
data class NotificationItem(
    val id: String,
    val type: String,
    val title: String,
    val content: String,
    val orderId: String? = null,
    val read: Boolean,
    val createdAt: String,
)

@Serializable
data class NotificationResult(val list: List<NotificationItem>, val unread: Int)

@Serializable
data class AiRecommendation(
    val merchantId: String,
    val itemId: String,
    val name: String,
    val price: Double,
)

@Serializable
data class AiReply(val reply: String, val recommendations: List<AiRecommendation> = emptyList())
