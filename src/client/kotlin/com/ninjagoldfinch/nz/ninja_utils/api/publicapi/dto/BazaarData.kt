package com.ninjagoldfinch.nz.ninja_utils.api.publicapi.dto

data class BazaarResponse(
    val success: Boolean,
    val lastUpdated: Long,
    val products: Map<String, BazaarProduct>?
)

data class BazaarProduct(
    val product_id: String,
    val quick_status: BazaarQuickStatus?
)

data class BazaarQuickStatus(
    val productId: String?,
    val sellPrice: Double,
    val sellVolume: Long,
    val sellMovingWeek: Long,
    val sellOrders: Int,
    val buyPrice: Double,
    val buyVolume: Long,
    val buyMovingWeek: Long,
    val buyOrders: Int
)
