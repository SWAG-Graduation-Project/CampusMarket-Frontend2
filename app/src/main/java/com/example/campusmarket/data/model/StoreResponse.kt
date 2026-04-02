package com.example.campusmarket.data.model

data class StoreResponse(
    val code: String,
    val message: String,
    val result: StoreResult,
    val success: Boolean
)

data class StoreResult(
    val stores: List<Store>?,
    val page: Int,
    val size: Int,
    val hasNext: Boolean
)