package com.example.campusmarket.data.model

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    val code: String? = null,
    val message: String? = null,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("success")
    val success: Boolean? = null
)

data class CreateProductResponse(
    val code: String? = null,
    val message: String? = null,
    @SerializedName("data")
    val data: CreateProductResult? = null,
    @SerializedName("result")
    val result: CreateProductResult? = null,
    val success: Boolean? = null
)