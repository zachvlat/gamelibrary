package com.zachvlat.gamelibrary.library.store.ea

import kotlinx.serialization.Serializable

@Serializable
data class EaOrderHistoryResponse(
    val result: EaResult? = null
)

@Serializable
data class EaResult(
    val orders: List<EaOrder> = emptyList()
)

@Serializable
data class EaOrder(
    val date: String? = null,
    val description: String? = null,
    val status: String? = null,
    val items: List<EaOrderItem> = emptyList()
)

@Serializable
data class EaOrderItem(
    val name: String? = null,
    val thumbnail: String? = null,
    val platform: String? = null,
    val status: String? = null,
    val itemType: String? = null
)
