package com.example.campusmarket

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.campusmarket.data.LoungeImageData
import com.example.campusmarket.data.LockerGroupData
import com.example.campusmarket.data.SelectedLockerGroup

class LockerGroupPopupDialogFragment(
    private val buildingName: String,
    private val floor: Int,
    private val imageIndex: Int,
    private val loungeImages: List<LoungeImageData>,
    private val lockerGroups: List<LockerGroupData>,
    private val onLockerGroupSelected: (SelectedLockerGroup) -> Unit
) : DialogFragment() {

    private lateinit var loungeOverlay: FrameLayout
    private lateinit var loungeContainer: FrameLayout
    private lateinit var loungeImageStage: FrameLayout
    private lateinit var imgLounge: ImageView
    private lateinit var btnCloseLounge: ImageButton

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_locker_group_popup)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        loungeOverlay = dialog.findViewById(R.id.loungeOverlay)
        loungeContainer = dialog.findViewById(R.id.loungeContainer)
        loungeImageStage = dialog.findViewById(R.id.loungeImageStage)
        imgLounge = dialog.findViewById(R.id.imgLounge)
        btnCloseLounge = dialog.findViewById(R.id.btnCloseLounge)

        btnCloseLounge.setOnClickListener { dismiss() }
        loungeOverlay.setOnClickListener { dismiss() }
        loungeContainer.setOnClickListener {
            // 내부 클릭 시 닫히지 않음
        }

        showLoungeImage()

        return dialog
    }

    private fun showLoungeImage() {
        val loungeImage = loungeImages.find {
            it.buildingName == buildingName &&
                    it.floor == floor &&
                    it.imageIndex == imageIndex
        }

        if (loungeImage == null) {
            Toast.makeText(
                requireContext(),
                "배경 이미지가 없습니다: $buildingName ${floor}층 ${imageIndex}번",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
            return
        }

        imgLounge.setImageResource(loungeImage.imageResId)

        loungeImageStage.post {
            removeLockerViews()

            val currentGroups = lockerGroups.filter {
                it.buildingName == buildingName &&
                        it.floor == floor &&
                        it.imageIndex == imageIndex
            }

            addLockerGroups(currentGroups)
        }
    }

    private fun addLockerGroups(groups: List<LockerGroupData>) {
        groups.forEach { addLockerGroup(it) }
    }

    private fun addLockerGroup(lockerData: LockerGroupData) {
        val (displayX, displayY) = convertToDisplayPosition(
            lockerData.originalX,
            lockerData.originalY
        )

        val locker = ImageView(requireContext())
        locker.setImageResource(lockerData.lockerImageResId)

        val lockerWidth = (loungeImageStage.width * 0.08f).toInt()
        val lockerHeight = (lockerWidth * 1.2f).toInt()

        val params = FrameLayout.LayoutParams(lockerWidth, lockerHeight)
        locker.layoutParams = params

        val (finalX, finalY) = applyLockerOffset(
            displayX = displayX,
            displayY = displayY,
            lockerWidth = lockerWidth,
            lockerHeight = lockerHeight
        )

        locker.x = finalX
        locker.y = finalY

        locker.setOnClickListener {
            resetLockerSelection()

            locker.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(150)
                .start()

            locker.elevation = 20f

            val selected = SelectedLockerGroup(
                buildingName = lockerData.buildingName,
                floor = lockerData.floor,
                major = lockerData.major,
                groupNumber = lockerData.groupNumber,
                rowCount = lockerData.rowCount,
                colCount = lockerData.colCount,
                frontImageResId = lockerData.frontImageResId
            )

            onLockerGroupSelected(selected)

            Toast.makeText(
                requireContext(),
                "${lockerData.major} / ${lockerData.groupNumber}번 그룹 선택",
                Toast.LENGTH_SHORT
            ).show()

            dismiss()
        }

        loungeImageStage.addView(locker)
    }

    private fun resetLockerSelection() {
        for (i in 0 until loungeImageStage.childCount) {
            val child = loungeImageStage.getChildAt(i)
            if (child is ImageView && child.id != R.id.imgLounge) {
                child.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
                child.elevation = 0f
            }
        }
    }

    private fun convertToDisplayPosition(
        originalX: Float,
        originalY: Float
    ): Pair<Float, Float> {
        val originalWidth = 2000f
        val originalHeight = 1500f

        val stageWidth = loungeImageStage.width.toFloat()
        val stageHeight = loungeImageStage.height.toFloat()

        val displayX = (originalX / originalWidth) * stageWidth
        val displayY = (originalY / originalHeight) * stageHeight

        return Pair(displayX, displayY)
    }

    private fun applyLockerOffset(
        displayX: Float,
        displayY: Float,
        lockerWidth: Int,
        lockerHeight: Int
    ): Pair<Float, Float> {
        val finalX = displayX - lockerWidth / 2f
        val finalY = displayY - lockerHeight / 2f
        return Pair(finalX, finalY)
    }

    private fun removeLockerViews() {
        for (i in loungeImageStage.childCount - 1 downTo 0) {
            val child = loungeImageStage.getChildAt(i)
            if (child.id != R.id.imgLounge) {
                loungeImageStage.removeViewAt(i)
            }
        }
    }
}