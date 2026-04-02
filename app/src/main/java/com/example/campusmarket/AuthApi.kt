package com.example.campusmarket.network

import com.example.campusmarket.model.GuestRequest
import com.example.campusmarket.model.GuestResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {

    @Headers("Content-Type: application/json")
    @POST("auth/guest")
    suspend fun createGuest(
        @Body request: GuestRequest
    ): Response<GuestResponse>
}