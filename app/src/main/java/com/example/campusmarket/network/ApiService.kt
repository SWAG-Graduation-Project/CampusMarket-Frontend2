package com.example.campusmarket.network

import com.example.campusmarket.data.model.ChatMessagesResponse
import com.example.campusmarket.data.model.ChatRoomRequest
import com.example.campusmarket.data.model.ChatRoomResponse
import com.example.campusmarket.data.model.MajorCategoryResponse
import com.example.campusmarket.data.model.ProductDetailResponse
import com.example.campusmarket.data.model.ProposalListResponse
import com.example.campusmarket.data.model.ProposalRequest
import com.example.campusmarket.data.model.ProposalRespondRequest
import com.example.campusmarket.data.model.ProposalResponse
import com.example.campusmarket.data.model.SellingChatRoomsResponse
import com.example.campusmarket.data.model.StoreResponse
import com.example.campusmarket.data.model.UserMarketProductsResponse
import com.example.campusmarket.data.model.UserStoreDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("categories/major")
    suspend fun getMajorCategories(): MajorCategoryResponse

    @GET("stores")
    suspend fun getStores(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): StoreResponse

    @POST("chat/rooms")
    suspend fun createOrEnterChatRoom(
        @Header("guestUuid") guestUuid: String,
        @Body request: ChatRoomRequest
    ): Response<ChatRoomResponse>

    @GET("chat/rooms/selling")
    suspend fun getSellingChatRooms(
        @Header("guestUuid") guestUuid: String
    ): Response<SellingChatRoomsResponse>

    @GET("chat/rooms/buying")
    suspend fun getBuyingChatRooms(
        @Header("guestUuid") guestUuid: String
    ): Response<SellingChatRoomsResponse>

    @GET("chat/rooms/{chatRoomId}/messages")
    suspend fun getChatMessages(
        @Header("guestUuid") guestUuid: String,
        @Path("chatRoomId") chatRoomId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<ChatMessagesResponse>

    @POST("chat/rooms/{chatRoomId}/proposals")
    suspend fun createProposal(
        @Header("guestUuid") guestUuid: String,
        @Path("chatRoomId") chatRoomId: Long,
        @Body request: ProposalRequest
    ): Response<ProposalResponse>

    @GET("chat/rooms/{chatRoomId}/proposals")
    suspend fun getProposals(
        @Header("guestUuid") guestUuid: String,
        @Path("chatRoomId") chatRoomId: Long
    ): Response<ProposalListResponse>

    @PATCH("chat/rooms/{chatRoomId}/proposals/{proposalId}")
    suspend fun respondToProposal(
        @Header("guestUuid") guestUuid: String,
        @Path("chatRoomId") chatRoomId: Long,
        @Path("proposalId") proposalId: Long,
        @Body request: ProposalRespondRequest
    ): Response<ProposalResponse>

    @GET("products/{productId}")
    suspend fun getProductDetail(
        @Path("productId") productId: Long
    ): ProductDetailResponse

    @GET("stores/{sellerId}")
    suspend fun getStoreDetail(
        @Path("sellerId") sellerId: Long
    ): UserStoreDetailResponse

    @GET("stores/{sellerId}/products")
    suspend fun getStoreProducts(
        @Path("sellerId") sellerId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): UserMarketProductsResponse
}