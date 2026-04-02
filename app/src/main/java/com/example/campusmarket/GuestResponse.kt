package com.example.campusmarket.model

data class GuestResponse(
    val code: String,
    val message: String,
    val result: GuestResult,
    val success: Boolean
)