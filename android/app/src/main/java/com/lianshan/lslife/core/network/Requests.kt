package com.lianshan.lslife.core.network

import kotlinx.serialization.Serializable

@Serializable
data class SendCodeRequest(val phone: String)

@Serializable
data class LoginRequest(val phone: String, val code: String)

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
    val title: String,
    val description: String,
    val price: Double? = null,
    val images: List<String> = emptyList(),
)

@Serializable
data class SubscribeRequest(val tier: String)

@Serializable
data class AiRequest(val prompt: String)
