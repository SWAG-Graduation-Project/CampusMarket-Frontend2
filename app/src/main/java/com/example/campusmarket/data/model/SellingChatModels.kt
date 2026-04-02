package com.example.campusmarket.data.model

data class SellingChatRoomsResponse(
    val code: String,
    val message: String,
    val result: SellingChatRoomsResult?,
    val success: Boolean
)

data class SellingChatRoomsResult(
    val chatRooms: List<SellingChatRoom>
)

data class SellingChatRoom(
    val chatRoomId: Long,
    val productId: Long?,
    val productName: String?,
    val productThumbnailUrl: String?,
    val isSeller: Boolean,
    val sellerId: Long?,
    val sellerNickname: String?,
    val sellerProfileImageUrl: String?,
    val buyerId: Long?,
    val buyerNickname: String?,
    val buyerProfileImageUrl: String?,
    val lastMessageContent: String?,
    val lastMessageAt: String?,
    val createdAt: String?,
    val status: String?
)

data class ChatSendRequest(
    val guestUuid: String,
    val messageType: String,
    val content: String,
    val metadata: String? = null
)

data class ChatReceiveDto(
    val messageId: Long?,
    val senderId: Long?,
    val senderNickname: String?,
    val messageType: String?,
    val content: String?,
    val metadata: String?,
    val createdAt: String?,
    val isDeleted: Boolean?
)

data class ChatMessagesResponse(
    val code: String,
    val message: String,
    val result: ChatMessagesResult?,
    val success: Boolean
)

data class ChatMessagesResult(
    val messages: List<ChatReceiveDto>,
    val page: Int,
    val size: Int,
    val totalCount: Int,
    val hasNext: Boolean
)
