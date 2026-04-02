package com.example.campusmarket.data

data class LoungeImageData(
    val buildingName: String,
    val floor: Int,
    val imageIndex: Int,
    val imageResId: Int
)

data class LockerGroupData(
    val buildingName: String,
    val floor: Int,
    val imageIndex: Int,
    val originalX: Float,
    val originalY: Float,
    val major: String,
    val groupNumber: Int,
    val lockerImageResId: Int,

    val rowCount: Int,
    val colCount: Int,

    // 이제 "전체 정면샷"이 아니라 "한 칸짜리 정면 이미지"
    val frontImageResId: Int
)

data class LockerCellData(
    val buildingName: String,
    val floor: Int,
    val major: String,
    val lockerGroup: Int,
    val row: Int,
    val col: Int
)

data class SelectedLockerGroup(
    val buildingName: String,
    val floor: Int,
    val major: String,
    val groupNumber: Int,
    val rowCount: Int,
    val colCount: Int,
    val frontImageResId: Int
)