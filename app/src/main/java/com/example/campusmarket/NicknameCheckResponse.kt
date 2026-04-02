package com.example.campusmarket.model

data class NicknameCheckResponse(
    val code: String,
    val message: String,
    val result: NicknameCheckResult,
    val success: Boolean
)

data class NicknameCheckResult(
    val nickname: String,
    val available: Boolean
)