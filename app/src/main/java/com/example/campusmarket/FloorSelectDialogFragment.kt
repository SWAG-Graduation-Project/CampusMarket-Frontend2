package com.example.campusmarket

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class FloorSelectDialogFragment(
    private val buildingName: String,
    private val onFloorSelected: (Int) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setDimAmount(0.55f)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_floor_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupFloor)
        val rbFloor1 = view.findViewById<RadioButton>(R.id.rbFloor1)
        val rbFloor2 = view.findViewById<RadioButton>(R.id.rbFloor2)
        val rbFloor3 = view.findViewById<RadioButton>(R.id.rbFloor3)
        val rbFloor4 = view.findViewById<RadioButton>(R.id.rbFloor4)
        val btnMove = view.findViewById<Button>(R.id.btnMove)

        tvTitle.text = "${buildingName} 층을 선택하세요"

        val availableFloors = when (buildingName) {
            "차관" -> listOf(1, 2, 3, 4)
            "인문대" -> listOf(1, 2, 3)
            "자연대" -> listOf(2, 3, 4)
            "예술대학" -> listOf(1, 2)
            else -> listOf(1, 2, 3, 4)
        }

        rbFloor1.visibility = if (1 in availableFloors) View.VISIBLE else View.GONE
        rbFloor2.visibility = if (2 in availableFloors) View.VISIBLE else View.GONE
        rbFloor3.visibility = if (3 in availableFloors) View.VISIBLE else View.GONE
        rbFloor4.visibility = if (4 in availableFloors) View.VISIBLE else View.GONE

        when {
            1 in availableFloors -> rbFloor1.isChecked = true
            2 in availableFloors -> rbFloor2.isChecked = true
            3 in availableFloors -> rbFloor3.isChecked = true
            4 in availableFloors -> rbFloor4.isChecked = true
        }

        btnMove.setOnClickListener {
            val selectedFloor = when (radioGroup.checkedRadioButtonId) {
                R.id.rbFloor1 -> 1
                R.id.rbFloor2 -> 2
                R.id.rbFloor3 -> 3
                R.id.rbFloor4 -> 4
                else -> availableFloors.firstOrNull() ?: 1
            }

            onFloorSelected(selectedFloor)
            dismiss()
        }
    }
}