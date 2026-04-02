package com.example.campusmarket.data.model

data class BackgroundRemovalRequest(
    val tempImageIds: List<Long>
)

data class BackgroundRemovalResponse(
    val code: String,
    val message: String,
    val result: BackgroundRemovalResult?,
    val success: Boolean
)

data class BackgroundRemovalResult(
    val items: List<BackgroundRemovalItem>
)

data class BackgroundRemovalItem(
    val tempImageId: Long,
    val backgroundRemovedImageUrl: String?
)