package com.example.campusmarket.data.model

data class TempImageUploadResponse(
    val code: String,
    val message: String,
    val result: TempImageResult?,
    val success: Boolean
)

data class TempImageResult(
    val tempImageId: Long,
    val originalImageUrl: String?,
    val backgroundRemovedImageUrl: String?,
    val backgroundRemoved: Boolean,
    val displayOrder: Int
)