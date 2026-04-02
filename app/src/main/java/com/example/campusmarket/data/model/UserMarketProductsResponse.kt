package com.example.campusmarket.data.model

data class UserMarketProductsResponse(
    val code: String,
    val message: String,
    val result: UserMarketProductsResult,
    val success: Boolean
)

data class UserMarketProductsResult(
    val products: List<UserMarketProduct>,
    val page: Int,
    val size: Int,
    val hasNext: Boolean
)