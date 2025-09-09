package com.example.intervaltimer

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.GridView
import androidx.fragment.app.DialogFragment

class ColorPickerDialog : DialogFragment() {
    interface OnColorSelectedListener {
        fun onColorSelected(color: Int)
    }

    private var listener: OnColorSelectedListener? = null
    private var selectedColor: Int = Color.BLACK

    fun setListener(listener: OnColorSelectedListener) {
        this.listener = listener
    }

    fun setInitialColor(color: Int) {
        selectedColor = color
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_color_picker, null)
        val gridView = view.findViewById<GridView>(R.id.colorGrid)

        // Определяем цвета для выбора
        val colors = intArrayOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.MAGENTA, Color.GRAY, Color.WHITE,
            Color.BLACK, Color.parseColor("#FFA500"), // Orange
            Color.parseColor("#800080"), // Purple
            Color.parseColor("#FFC0CB")  // Pink
        )

        val adapter = ColorAdapter(requireContext(), colors, selectedColor)
        gridView.adapter = adapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            selectedColor = colors[position]
            adapter.setSelectedColor(selectedColor)
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Выберите цвет")
            .setPositiveButton("OK") { _, _ ->
                listener?.onColorSelected(selectedColor)
            }
            .setNegativeButton("Отмена", null)
            .create()
    }
}