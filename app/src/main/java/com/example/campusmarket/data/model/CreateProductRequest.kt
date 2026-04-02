package com.example.campusmarket.data.model

data class CreateProductRequest(
    val majorCategoryId: Long,
    val subCategoryId: Long,
    val name: String,
    val brand: String,
    val color: String,
    val productCondition: String,
    val description: String,
    val price: Int,
    val isFree: Boolean,
    val images: List<CreateProductImageRequest>
)

data class CreateProductImageRequest(
    val imageUrl: String
)