package com.example.intervaltimer

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.example.intervaltimer.databinding.DialogAddExerciseBinding
import kotlin.random.Random

class AddExerciseDialog : DialogFragment() {
    interface OnExerciseAddedListener {
        fun onExerciseAdded(exercise: Exercise)
    }

    private var _binding: DialogAddExerciseBinding? = null
    private val binding get() = _binding!!
    private var listener: OnExerciseAddedListener? = null
    private var selectedColor: Int = Color.BLACK

    fun setListener(listener: OnExerciseAddedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddExerciseBinding.inflate(requireActivity().layoutInflater)

        // Настройка предпросмотра цвета
        updateColorPreview()

        // Обработчик кнопки выбора цвета
        binding.selectColorButton.setOnClickListener {
            showColorPickerDialog()
        }

        val builder = AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setTitle("Добавить упражнение")
            .setPositiveButton("Добавить") { dialog, which ->
                addExercise()
            }
            .setNegativeButton("Отмена", null)

        return builder.create()
    }

    private fun showColorPickerDialog() {
        val dialog = ColorPickerDialog()
        dialog.setInitialColor(selectedColor)
        dialog.setListener(object : ColorPickerDialog.OnColorSelectedListener {
            override fun onColorSelected(color: Int) {
                selectedColor = color
                updateColorPreview()
            }
        })
        dialog.show(parentFragmentManager, "ColorPickerDialog")
    }

    private fun updateColorPreview() {
        binding.colorPreview.setBackgroundColor(selectedColor)
    }

    private fun addExercise() {
        var name = binding.exerciseNameEditText.text.toString()
        val minutesText = binding.exerciseMinutesEditText.text.toString()
        val secondsText = binding.exerciseSecondsEditText.text.toString()



        // Если название пустое, задаем значение по умолчанию
        if (name.isBlank()) {
            name = "Элемент"
        }

        // Валидация минут и секунд (используем toLongOrNull вместо toIntOrNull)
        val minutes = minutesText.toLongOrNull() ?: 0L
        val seconds = secondsText.toLongOrNull() ?: 0L

        // Проверяем ограничения
        if (minutes > 99) {
            binding.exerciseMinutesEditText.error = "Максимум 99 минут"
            return
        }

        if (seconds > 59) {
            binding.exerciseSecondsEditText.error = "Максимум 59 секунд"
            return
        }

        if (minutes == 0L && seconds == 0L) {
            binding.exerciseMinutesEditText.error = "Введите время"
            return
        }

        val durationMs = (minutes * 60 + seconds) * 1000

        val selectedType = when (binding.typeRadioGroup.checkedRadioButtonId) {
            R.id.prepRadioButton -> ElementType.PREP
            R.id.workRadioButton -> ElementType.WORK
            R.id.restRadioButton -> ElementType.REST
            else -> ElementType.WORK
        }

        val exercise = Exercise(
            id = System.currentTimeMillis() + Random.nextLong(1000),
            name = name,
            type = selectedType,
            duration = durationMs,
            color = selectedColor
        )

        listener?.onExerciseAdded(exercise)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}