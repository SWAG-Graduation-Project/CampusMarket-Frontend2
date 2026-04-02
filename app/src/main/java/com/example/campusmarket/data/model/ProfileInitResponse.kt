package com.example.campusmarket.model

data class ProfileInitResponse(
    val code: String,
    val message: String,
    val result: ProfileInitResult?,
    val success: Boolean
)

data class ProfileInitResult(
    val memberId: Long,
    val guestUuid: String,
    val nickname: String,
    val profileImageUrl: String,
    val lockerName: String,
    val timetableData: String,
    val profileCompleted: Boolean
)