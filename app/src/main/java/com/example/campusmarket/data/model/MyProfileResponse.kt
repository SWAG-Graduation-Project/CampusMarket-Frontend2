package com.example.campusmarket.data.model

data class MyProfileResponse(
    val code: String,
    val message: String,
    val result: MyProfileData?,
    val success: Boolean
)

data class MyProfileData(
    val memberId: Long,
    val guestUuid: String?,
    val nickname: String?,
    val profileImageUrl: String?,
    val lockerName: String?,
    val timetableData: String?,
    val profileCompleted: Boolean
)
