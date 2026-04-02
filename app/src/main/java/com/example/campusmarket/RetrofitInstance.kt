package com.example.campusmarket

import com.example.campusmarket.network.MemberApi   // ← 너 package에 맞게 수정

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://너의서버주소.com"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val memberApi: MemberApi by lazy {
        retrofit.create(MemberApi::class.java)
    }
}