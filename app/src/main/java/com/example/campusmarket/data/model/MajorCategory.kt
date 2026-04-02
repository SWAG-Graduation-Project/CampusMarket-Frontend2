package com.example.campusmarket.data.model

data class MajorCategoryResponse(
    val code: String,
    val message: String,
    val result: MajorCategoryResult,
    val success: Boolean
)

data class MajorCategoryResult(
    val majorCategories: List<MajorCategory>
)

data class MajorCategory(
    val majorCategoryId: Long,
    val name: String,
    val iconUrl: String?,
    val sortOrder: Int
)