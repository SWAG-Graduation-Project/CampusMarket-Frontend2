package com.example.campusmarket.data.model

import com.google.gson.annotations.SerializedName

data class ProductDraftRequest(
    val tempImageIds: List<Long>
)

data class ProductDraftResponse(
    val code: String?,
    val message: String?,
    @SerializedName(value = "result", alternate = ["data"])
    val result: ProductDraftResult?,
    val success: Boolean?
)

data class ProductDraftResult(
    val majorCategoryId: Long?,
    val majorCategoryName: String?,
    val subCategoryId: Long?,
    val subCategoryName: String?,
    val productName: String?,
    val color: String?,
    val productCondition: String?,
    val description: String?
)