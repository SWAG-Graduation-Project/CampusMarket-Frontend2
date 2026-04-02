package com.example.campusmarket.data.model

import com.google.gson.annotations.SerializedName

data class ParsedTimetable(
    @SerializedName("classes")
    val classes: List<TimetableClass>
)

data class TimetableClass(
    @SerializedName("name")
    val name: String?,

    @SerializedName("day")
    val day: String,

    @SerializedName("start_time")
    val startTime: String,

    @SerializedName("end_time")
    val endTime: String,

    @SerializedName("location")
    val location: String?
)