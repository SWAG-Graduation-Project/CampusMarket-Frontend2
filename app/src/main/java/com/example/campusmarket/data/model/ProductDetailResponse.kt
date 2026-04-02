package com.example.campusmarket.data.model

data class ProductDetailResponse(
    val code: String,
    val message: String,
    val result: ProductDetailResult,
    val success: Boolean
)

data class ProductDetailResult(
    val productId: Long,
    val name: String,
    val brand: String?,
    val color: String?,
    val productCondition: String,
    val description: String?,
    val price: Int,
    val isFree: Boolean,
    val saleStatus: String,
    val viewCount: Int,
    val wishCount: Int,
    val displayAssetImageUrl: String?,
    val createdAt: String?,
    val category: ProductCategory?,
    val seller: ProductSeller?,
    val images: List<ProductImage>?,
    val isWished: Boolean,
    val canChat: Boolean
)

data class ProductCategory(
    val majorCategoryId: Long,
    val majorCategoryName: String,
    val subCategoryId: Long,
    val subCategoryName: String
)

data class ProductSeller(
    val sellerId: Long,
    val nickname: String?,
    val profileImageUrl: String?,
    val storeStartedAt: String?,
    val saleCount: Int
)

data class ProductImage(
    val productImageId: Long,
    val imageUrl: String?,
    val originalImageUrl: String?,
    val backgroundRemoved: Boolean,
    val displayOrder: Int
)