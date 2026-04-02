package com.example.campusmarket.data

import com.example.campusmarket.R

object LockerDataSource {

    val loungeImageList = listOf(
        LoungeImageData("차관", 1, 1, R.drawable.cha_center1),
        LoungeImageData("차관", 2, 1, R.drawable.cha_center2),
        LoungeImageData("차관", 3, 1, R.drawable.cha_center3),
        LoungeImageData("차관", 4, 1, R.drawable.cha_center4)
    )

    val lockerList = listOf(
        LockerGroupData(
            buildingName = "차관",
            floor = 1,
            imageIndex = 1,
            originalX = 637f+110f,
            originalY = 915f-35f,
            major = "경영",
            groupNumber = 1,
            lockerImageResId = R.drawable.lockerback1,
            rowCount = 4,
            colCount = 12,
            frontImageResId = R.drawable.locker_one
        ),
        LockerGroupData(
            buildingName = "차관",
            floor = 1,
            imageIndex = 1,
            originalX = 795f + 110f,
            originalY = 907f - 25f,
            major = "경영",
            groupNumber = 2,
            lockerImageResId = R.drawable.lockerback2,
            rowCount = 4,
            colCount = 12,
            frontImageResId = R.drawable.locker_one
        ),
                LockerGroupData(
                buildingName = "차관",
        floor = 1,
        imageIndex = 1,
        originalX = 993f + 110f,
        originalY = 864f - 35f,
        major = "경영",
        groupNumber = 3,
        lockerImageResId = R.drawable.lockerback1,
        rowCount = 4,
        colCount = 6,
        frontImageResId = R.drawable.locker_one
    ),
        LockerGroupData(
            buildingName = "차관",
            floor = 1,
            imageIndex = 1,
            originalX = 1124f + 130f,
            originalY = 800f,
            major = "경영",
            groupNumber = 4,
            lockerImageResId = R.drawable.lockerback1,
            rowCount = 4,
            colCount = 6,
            frontImageResId = R.drawable.locker_one
        ),
        LockerGroupData(
            buildingName = "차관",
            floor = 1,
            imageIndex = 1,
            originalX = 1648f + 110f,
            originalY = 740f - 35f,
            major = "수학과",
            groupNumber = 3,
            lockerImageResId = R.drawable.lockerfrontleft,
            rowCount = 4,
            colCount = 12,
            frontImageResId = R.drawable.locker_one
        )
    )

    fun getLockerGroups(
        buildingName: String,
        floor: Int,
        imageIndex: Int
    ): List<LockerGroupData> {
        return lockerList.filter {
            it.buildingName == buildingName &&
                    it.floor == floor &&
                    it.imageIndex == imageIndex
        }
    }

    fun createLockerCells(group: LockerGroupData): List<LockerCellData> {
        val result = mutableListOf<LockerCellData>()

        for (row in 1..group.rowCount) {
            for (col in 1..group.colCount) {
                result.add(
                    LockerCellData(
                        buildingName = group.buildingName,
                        floor = group.floor,
                        major = group.major,
                        lockerGroup = group.groupNumber,
                        row = row,
                        col = col
                    )
                )
            }
        }

        return result
    }
}