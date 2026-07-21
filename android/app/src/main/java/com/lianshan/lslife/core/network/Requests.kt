package com.lianshan.lslife.core.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val phone: String, val password: String)

@Serializable
data class RegisterRequest(val phone: String, val email: String, val password: String, val nickname: String? = null)

@Serializable
data class SendEmailCodeRequest(val email: String)

@Serializable
data class ResetPasswordRequest(val email: String, val code: String, val newPassword: String)

@Serializable
data class ChangePasswordRequest(val oldPassword: String, val newPassword: String)

@Serializable
data class RealNameRequest(val realName: String, val idCard: String)

@Serializable
data class CartUpsertRequest(val productId: String, val quantity: Int)

@Serializable
data class DeliveryAddressBody(val name: String, val phone: String, val address: String)

@Serializable
data class OrderItemRequest(val productId: String, val quantity: Int)

@Serializable
data class CreateOrderRequest(
    val merchantId: String,
    val items: List<OrderItemRequest>,
    val deliveryAddress: DeliveryAddressBody,
)

@Serializable
data class CreatePaymentRequest(val orderId: String, val channel: String)

@Serializable
data class MockConfirmRequest(val orderNo: String)

@Serializable
data class AddressBody(
    val name: String,
    val phone: String,
    val tag: String = "家",
    val address: String,
    val isDefault: Boolean = false,
)

@Serializable
data class CreatePostRequest(
    val category: String,
    val title: String? = null,
    val description: String,
    val price: Double? = null,
    val images: List<String> = emptyList(),
    val publisherType: String = "INDIVIDUAL",
    val merchantId: String? = null,
    val listingType: String = "GOODS",
    val attributes: Map<String, String> = emptyMap(),
    val locationName: String? = null,
)

@Serializable
data class SubscribeRequest(val tier: String)

@Serializable
data class AiRequest(val prompt: String)

@Serializable
data class AiGenerateDescRequest(
    val title: String? = null,
    val category: String? = null,
    /** 用户已填写的原文，AI 在此基础上润色，不整段替换清空 */
    val draft: String? = null,
)

@Serializable
data class UploadResult(val url: String)
