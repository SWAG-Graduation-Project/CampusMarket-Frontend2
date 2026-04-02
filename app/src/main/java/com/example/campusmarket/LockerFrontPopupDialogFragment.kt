package com.example.campusmarket

import android.app.Dialog
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.campusmarket.data.LockerCellData

class LockerFrontPopupDialogFragment(
    private val lockerCellImageResId: Int,
    private val rowCount: Int,
    private val colCount: Int,
    private val lockerCells: List<LockerCellData>,
    private val onLockerCellClick: (LockerCellData) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val root = FrameLayout(requireContext()).apply {
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        val horizontalScrollView = HorizontalScrollView(requireContext()).apply {
            isHorizontalScrollBarEnabled = true
            isFillViewport = false
        }

        val verticalScrollView = ScrollView(requireContext()).apply {
            isVerticalScrollBarEnabled = true
            isFillViewport = false
        }

        val gridLayout = GridLayout(requireContext()).apply {
            rowCount = this@LockerFrontPopupDialogFragment.rowCount
            columnCount = this@LockerFrontPopupDialogFragment.colCount
            setPadding(0, 0, 0, 0)
            useDefaultMargins = false
        }

        lockerCells.forEach { cell ->
            gridLayout.addView(createLockerItem(cell))
        }

        verticalScrollView.addView(gridLayout)
        horizontalScrollView.addView(verticalScrollView)

        val scrollParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        root.addView(horizontalScrollView, scrollParams)

        val closeButton = ImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            background = null
            layoutParams = FrameLayout.LayoutParams(dp(36), dp(36)).apply {
                gravity = Gravity.TOP or Gravity.END
            }
            setOnClickListener { dismiss() }
        }
        root.addView(closeButton)

        dialog.setContentView(root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }

    private fun createLockerItem(cell: LockerCellData): FrameLayout {
        val itemWidth = dp(34)
        val itemHeight = dp(78)

        val container = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(itemWidth, itemHeight).apply {
                setMargins(0, 0, 0, 0)
            }
            setPadding(0, 0, 0, 0)
            isClickable = true
            isFocusable = true
        }

        val imageView = ImageView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setImageResource(lockerCellImageResId)
            scaleType = ImageView.ScaleType.FIT_XY
        }

        val label = TextView(requireContext()).apply {
            text = "${cell.row}-${cell.col}"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(2)
            }
        }

        container.addView(imageView)
        container.addView(label)

        container.setOnClickListener {
            onLockerCellClick(cell)
            dismiss()
        }

        return container
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}