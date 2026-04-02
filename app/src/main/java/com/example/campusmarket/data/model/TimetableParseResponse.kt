package com.example.campusmarket.data.model

import com.google.gson.JsonElement

data class TimetableParseResponse(
    val code: String,
    val message: String,
    val result: TimetableParseResult?,
    val success: Boolean
)

data class TimetableParseResult(
    val timetableData: JsonElement?
)