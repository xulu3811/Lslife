package com.lianshan.lslife.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val ORDERS = "orders"
    const val PUBLISH = "publish"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val MERCHANT = "merchant/{merchantId}"
    const val ORDER_TRACK = "order_track/{orderId}"

    fun merchant(id: String) = "merchant/$id"
    fun orderTrack(id: String) = "order_track/$id"
}
