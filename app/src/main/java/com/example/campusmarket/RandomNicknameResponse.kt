package com.example.campusmarket.model

data class RandomNicknameResponse(
    val code: String,
    val message: String,
    val result: RandomNicknameResult,
    val success: Boolean
)

data class RandomNicknameResult(
    val nickname: String
)