package com.example.campusmarket.data.model

data class UserStoreDetailResponse(
    val code: String,
    val message: String,
    val result: UserStoreDetailData,
    val success: Boolean
)

data class UserStoreDetailData(
    val sellerId: Long,
    val nickname: String?,
    val profileImageUrl: String?,
    val storeStartAt: String?,
    val saleCount: Int,
    val purchaseCount: Int,
    val totalProductCount: Int
)