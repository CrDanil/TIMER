package com.example.intervaltimer

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.intervaltimer.databinding.DialogAddExerciseBinding

class EditExerciseDialog : DialogFragment() {
    interface OnExerciseEditedListener {
        fun onExerciseEdited(updatedExercise: Exercise)
    }

    private var _binding: DialogAddExerciseBinding? = null
    private val binding get() = _binding!!
    private var listener: OnExerciseEditedListener? = null
    private lateinit var exercise: Exercise

    companion object {
        fun newInstance(exercise: Exercise): EditExerciseDialog {
            val dialog = EditExerciseDialog()
            dialog.exercise = exercise
            return dialog
        }
    }

    fun setListener(listener: OnExerciseEditedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddExerciseBinding.inflate(requireActivity().layoutInflater)

        // Заполняем поля данными упражнения
        binding.exerciseNameEditText.setText(exercise.name)
        binding.exerciseMinutesEditText.setText((exercise.duration / 1000 / 60).toString())
        binding.exerciseSecondsEditText.setText((exercise.duration / 1000 % 60).toString())

        when (exercise.type) {
            ElementType.PREP -> binding.prepRadioButton.isChecked = true
            ElementType.WORK -> binding.workRadioButton.isChecked = true
            ElementType.REST -> binding.restRadioButton.isChecked = true
        }

        val builder = AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setTitle("Редактировать упражнение")
            .setPositiveButton("Сохранить") { dialog, which ->
                saveExerciseChanges()
            }
            .setNegativeButton("Отмена", null)

        return builder.create()
    }

    private fun saveExerciseChanges() {
        val name = binding.exerciseNameEditText.text.toString()
        val minutesText = binding.exerciseMinutesEditText.text.toString()
        val secondsText = binding.exerciseSecondsEditText.text.toString()

        if (name.isBlank() || (minutesText.isBlank() && secondsText.isBlank())) {
            return
        }

        val minutes = minutesText.toLongOrNull() ?: 0
        val seconds = secondsText.toLongOrNull() ?: 0

        if (minutes == 0L && seconds == 0L) {
            return
        }

        val durationMs = (minutes * 60 + seconds) * 1000

        val selectedType = when (binding.typeRadioGroup.checkedRadioButtonId) {
            R.id.prepRadioButton -> ElementType.PREP
            R.id.workRadioButton -> ElementType.WORK
            R.id.restRadioButton -> ElementType.REST
            else -> ElementType.WORK
        }

        val updatedExercise = exercise.copy(
            name = name,
            type = selectedType,
            duration = durationMs
        )

        listener?.onExerciseEdited(updatedExercise)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}