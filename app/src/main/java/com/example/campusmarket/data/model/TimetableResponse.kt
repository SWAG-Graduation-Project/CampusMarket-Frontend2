package com.example.campusmarket.data.model

data class TimetableResponse(
    val code: String,
    val message: String,
    val result: TimetableResult?,
    val success: Boolean
)

data class TimetableResult(
    val timetableData: String?
)