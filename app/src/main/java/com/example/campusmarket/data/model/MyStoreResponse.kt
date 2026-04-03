package com.example.campusmarket.data.model

data class MyStoreResponse(
    val code: String,
    val message: String,
    val data: MyStoreData?
)

data class MyStoreData(
    val memberId: Long,
    val nickname: String?,
    val profileImageUrl: String?,
    val saleCount: Int,
    val purchaseCount: Int,
    val latestProducts: List<MyStoreLatestProduct>
)

data class MyStoreLatestProduct(
    val productId: Long,
    val productName: String,
    val price: Int,
    val thumbnailImageUrl: String?
)

data class MyStoreProductsResponse(
    val code: String,
    val message: String,
    val data: MyStoreProductsData?
)

data class MyStoreProductsData(
    val products: List<MyStoreProduct>
)

data class MyStoreProduct(
    val productId: Long,
    val productName: String,
    val price: Int,
    val isFree: Boolean,
    val saleStatus: String,
    val thumbnailImageUrl: String?,
    val viewCount: Int,
    val wishCount: Int,
    val createdAt: String
)
