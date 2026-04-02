package com.example.campusmarket

data class ChatMessage(
    val senderName: String,
    val message: String,
    val time: String,
    val isMine: Boolean,
    val messageType: String = "TEXT",
    val proposalId: Long? = null,
    val proposalType: String? = null,
    val proposalStatus: String? = null,
    val metadata: String? = null
)