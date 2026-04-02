package com.example.campusmarket.model

data class ProfileInitRequest(
    val nickname: String,
    val profileImageUrl: String,
    val lockerName: String,
    val timetableData: String
)