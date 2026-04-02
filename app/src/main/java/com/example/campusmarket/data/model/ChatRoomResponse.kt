package com.example.campusmarket.data.model

data class ChatRoomResponse(
    val code: String,
    val message: String,
    val result: ChatRoomResult,
    val success: Boolean
)

data class ChatRoomResult(
    val chatRoomId: Long,
    val isNew: Boolean
)