package com.example.campusmarket.network.dto
data class LockerSaveRequest(
    val building: String,
    val floor: Int,
    val major: String,
    val lockerGroup: Int,
    val row: Int,
    val col: Int
)