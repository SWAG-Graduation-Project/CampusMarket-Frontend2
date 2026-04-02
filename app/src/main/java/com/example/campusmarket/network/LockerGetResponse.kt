package com.example.campusmarket.network.dto

data class LockerGetResponse(
    val code: String,
    val message: String,
    val result: LockerGetResult?,
    val success: Boolean
)

data class LockerGetResult(
    val lockerName: String,
    val building: String,
    val floor: String,
    val major: String,
    val lockerGroup: Int,
    val row: Int,
    val col: Int
)