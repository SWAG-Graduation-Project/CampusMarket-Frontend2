package com.example.campusmarket.network.dto

data class LockerSaveResponse(
    val code: String,
    val message: String,
    val result: LockerSaveResult?,
    val success: Boolean
)

data class LockerSaveResult(
    val lockerName: String,
    val building: String,
    val floor: String,
    val major: String,
    val lockerGroup: Int,
    val row: Int,
    val col: Int
)