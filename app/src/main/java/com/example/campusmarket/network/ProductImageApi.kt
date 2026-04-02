package com.example.campusmarket.network

import com.example.campusmarket.data.model.BackgroundRemovalRequest
import com.example.campusmarket.data.model.BackgroundRemovalResponse
import com.example.campusmarket.data.model.BaseResponse
import com.example.campusmarket.data.model.CreateProductRequest
import com.example.campusmarket.data.model.CreateProductResponse
import com.example.campusmarket.data.model.CreateProductResult
import com.example.campusmarket.data.model.TempImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import com.example.campusmarket.data.model.ProductDraftRequest
import com.example.campusmarket.data.model.ProductDraftResponse


interface ProductImageApi {

    @Multipart
    @POST("products/images/temp")
    suspend fun uploadTempImage(
        @Header("X-Guest-UUID") guestUuid: String?,
        @Header("X-Member-Id") memberId: Long?,
        @Part files: MultipartBody.Part
    ): Response<TempImageUploadResponse>

    @POST("products/images/background-removal")
    suspend fun removeBackground(
        @Header("X-Guest-UUID") guestUuid: String,
        @Header("X-Member-Id") memberId: Long?,
        @Body request: BackgroundRemovalRequest
    ): Response<BackgroundRemovalResponse>
    @POST("products/draft")
    suspend fun createProductDraft(
        @Header("X-Guest-UUID") guestUuid: String?,
        @Header("X-Member-Id") memberId: Long?,
        @Body request: ProductDraftRequest
    ): Response<ProductDraftResponse>

    @POST("/api/products/my-store/products")
    suspend fun createMyStoreProduct(
        @Header("X-Guest-UUID") guestUuid: String,
        @Body request: CreateProductRequest
    ): Response<CreateProductResponse>


}