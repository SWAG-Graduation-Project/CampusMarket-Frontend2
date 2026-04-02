package com.example.campusmarket.data.model

data class Store(
    val sellerId: Long,
    val sellerNickname: String?,
    val latestProductId: Long?,
    val latestProductDisplayAssetImageUrl: String?,
    val latestProductCreatedAt: String?
)