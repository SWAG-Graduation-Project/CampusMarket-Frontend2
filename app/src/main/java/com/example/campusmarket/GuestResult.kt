package com.example.campusmarket.model

data class GuestResult(
    val memberId: Long,
    val guestUuid: String,
    val loginType: String,
    val memberStatus: String,
    val isNewMember: Boolean
)