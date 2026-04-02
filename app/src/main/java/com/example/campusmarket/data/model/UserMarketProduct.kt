package com.example.campusmarket.data.model

data class UserMarketProduct(
    val productId: Long,
    val sellerId: Long,
    val name: String,
    val price: Int,
    val isFree: Boolean,
    val saleStatus: String,
    val wishCount: Int,
    val thumbnailImageUrl: String?,
    val displayAssetImageUrl: String?,
    val createdAt: String
)