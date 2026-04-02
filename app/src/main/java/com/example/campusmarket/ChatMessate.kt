package com.example.campusmarket

data class ChatMessage(
    val senderName: String,
    val message: String,
    val time: String,
    val isMine: Boolean
)