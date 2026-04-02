package com.example.campusmarket.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.campusmarket.data.model.TimetableClass
import kotlin.math.max

class WeeklyTimetableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

    private val outerContainer = ScrollView(context)
    private val rootLayout = FrameLayout(context)

    private val dayHeaders = listOf("월", "화", "수", "목", "금", "토", "일")

    private val startHour = 9
    private val endHour = 21

    private val timeColumnWidth = dp(40)
    private val headerHeight = dp(28)
    private val hourHeight = dp(26)

    init {
        isHorizontalScrollBarEnabled = true
        outerContainer.isVerticalScrollBarEnabled = true

        outerContainer.addView(rootLayout)
        addView(outerContainer)
    }

    fun setTimetable(classes: List<TimetableClass>) {
        rootLayout.removeAllViews()

        val screenWidth = resources.displayMetrics.widthPixels
        val dayColumnWidth = ((screenWidth - timeColumnWidth) / dayHeaders.size).coerceAtLeast(dp(44))

        val totalWidth = timeColumnWidth + dayColumnWidth * dayHeaders.size
        val totalHeight = headerHeight + hourHeight * (endHour - startHour)

        val baseGrid = FrameLayout(context).apply {
            layoutParams = LayoutParams(totalWidth, totalHeight)
        }

        drawBackgroundGrid(baseGrid, totalWidth, totalHeight, dayColumnWidth)
        drawHeaders(baseGrid)
        drawTimeLabels(baseGrid)

        rootLayout.addView(baseGrid)

        classes.forEach { item ->
            val block = createClassBlock(item, dayColumnWidth)
            if (block != null) {
                rootLayout.addView(block)
            }
        }
    }

    private fun drawBackgroundGrid(parent: FrameLayout, totalWidth: Int, totalHeight: Int, dayColumnWidth: Int) {
        val verticalContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(totalWidth, totalHeight)
        }

        for (row in 0 until (endHour - startHour + 1)) {
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(totalWidth, if (row == 0) headerHeight else hourHeight)
            }

            if (row == 0) {
                rowLayout.addView(createCell(timeColumnWidth, headerHeight, ""))
                dayHeaders.forEach { day ->
                    rowLayout.addView(createCell(dayColumnWidth, headerHeight, day, isHeader = true))
                }
            } else {
                rowLayout.addView(createCell(timeColumnWidth, hourHeight, ""))
                repeat(dayHeaders.size) {
                    rowLayout.addView(createCell(dayColumnWidth, hourHeight, ""))
                }
            }

            verticalContainer.addView(rowLayout)
        }

        parent.addView(verticalContainer)
    }

    private fun drawHeaders(parent: FrameLayout) {
        // 이미 grid에서 그림
    }

    private fun drawTimeLabels(parent: FrameLayout) {
        for (hour in startHour until endHour) {
            val tv = TextView(context).apply {
                text = String.format("%02d:00", hour)
                setTextColor(Color.DKGRAY)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                setPadding(0, dp(4), 0, 0)
                layoutParams = FrameLayout.LayoutParams(timeColumnWidth, hourHeight).apply {
                    leftMargin = 0
                    topMargin = headerHeight + (hour - startHour) * hourHeight
                }
            }
            parent.addView(tv)
        }
    }

    private fun createCell(
        width: Int,
        height: Int,
        text: String,
        isHeader: Boolean = false
    ): TextView {
        return TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(width, height)
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(if (isHeader) Color.BLACK else Color.TRANSPARENT)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isHeader) 10f else 9f)
            setBackgroundColor(Color.WHITE)
            background = context.getDrawable(android.R.drawable.editbox_background)
        }
    }

    private fun createClassBlock(item: TimetableClass, dayColumnWidth: Int): TextView? {
        if (item.name.isNullOrBlank()) return null

        val dayIndex = dayHeaders.indexOf(item.day)
        if (dayIndex == -1) return null

        val startMinutes = convertToMinutes(item.startTime)
        val endMinutes = convertToMinutes(item.endTime)
        if (startMinutes == -1 || endMinutes == -1 || endMinutes <= startMinutes) return null

        val topBaseMinutes = startHour * 60
        val durationMinutes = endMinutes - startMinutes
        val offsetMinutes = startMinutes - topBaseMinutes

        if (offsetMinutes < 0) return null

        val top = headerHeight + (offsetMinutes * hourHeight / 60f).toInt()
        val height = max(dp(36), (durationMinutes * hourHeight / 60f).toInt())
        val left = timeColumnWidth + dayIndex * dayColumnWidth + dp(2)
        val width = dayColumnWidth - dp(4)

        val locationText = if (item.location.isNullOrBlank()) "" else "\n${item.location}"

        return TextView(context).apply {
            text = "${item.name}$locationText"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)
            gravity = Gravity.CENTER
            setPadding(dp(2), dp(4), dp(2), dp(4))
            setBackgroundColor(pickColor(item.name))
            layoutParams = FrameLayout.LayoutParams(width, height).apply {
                leftMargin = left
                topMargin = top
            }
        }
    }

    private fun convertToMinutes(time: String): Int {
        return try {
            val parts = time.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            hour * 60 + minute
        } catch (e: Exception) {
            -1
        }
    }

    private fun pickColor(seed: String): Int {
        val colors = listOf(
            Color.parseColor("#5C6BC0"),
            Color.parseColor("#26A69A"),
            Color.parseColor("#EF5350"),
            Color.parseColor("#FF7043"),
            Color.parseColor("#AB47BC"),
            Color.parseColor("#42A5F5"),
            Color.parseColor("#66BB6A")
        )
        return colors[kotlin.math.abs(seed.hashCode()) % colors.size]
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}