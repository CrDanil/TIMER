package com.example.intervaltimer

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.intervaltimer.databinding.DialogEditBlockBinding

class EditBlockDialog : DialogFragment() {
    interface OnBlockEditedListener {
        fun onBlockEdited(updatedBlock: Block)
    }

    private var _binding: DialogEditBlockBinding? = null
    private val binding get() = _binding!!
    private var listener: OnBlockEditedListener? = null
    private lateinit var block: Block
    private lateinit var elementsAdapter: WorkoutElementAdapter

    companion object {
        fun newInstance(block: Block): EditBlockDialog {
            val dialog = EditBlockDialog()
            val args = Bundle()
            args.putParcelable("block", block)
            dialog.arguments = args
            return dialog
        }
    }

    fun setListener(listener: OnBlockEditedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditBlockBinding.inflate(requireActivity().layoutInflater)
        block = arguments?.getParcelable("block") ?: throw IllegalArgumentException("Block required")

        // Настраиваем список элементов блока
        elementsAdapter = WorkoutElementAdapter(
            elements = block.elements,
            onItemClick = { element ->
                editElement(element)
            },
            onItemLongClick = { element ->
                showDeleteElementDialog(element)
                true
            }
        )

        binding.elementsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.elementsRecyclerView.adapter = elementsAdapter

        // Заполняем поля данными блока
        binding.blockNameEditText.setText(block.name)
        binding.blockRoundsEditText.setText(block.rounds.toString())

        // Кнопка добавления упражнения в блок
        binding.addElementButton.setOnClickListener {
            showAddExerciseDialog()
        }

        val builder = AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setTitle("Редактировать блок")
            .setPositiveButton("Сохранить") { dialog, which ->
                saveBlockChanges()
            }
            .setNegativeButton("Отмена", null)

        return builder.create()
    }

    private fun saveBlockChanges() {
        var name = binding.blockNameEditText.text.toString()
        val roundsText = binding.blockRoundsEditText.text.toString()

        if (name.isBlank()) {
            name = "Блок"
        }

        val rounds = roundsText.toIntOrNull() ?: return

        if (rounds <= 0) {
            return
        }

        // Вычисляем общую длительность блока
        val totalDuration = calculateTotalDuration(block.elements) * rounds

        val updatedBlock = block.copy(
            name = name,
            rounds = rounds,
            duration = totalDuration
        )

        listener?.onBlockEdited(updatedBlock)
        dismiss()
    }

    private fun calculateTotalDuration(elements: List<WorkoutElement>): Long {
        var totalDuration = 0L
        for (element in elements) {
            totalDuration += element.duration
        }
        return totalDuration
    }

    private fun showAddExerciseDialog() {
        val dialog = AddExerciseDialog()
        dialog.setListener(object : AddExerciseDialog.OnExerciseAddedListener {
            override fun onExerciseAdded(exercise: Exercise) {
                val updatedElements = block.elements.toMutableList().apply {
                    add(exercise)
                }
                block = block.copy(elements = updatedElements)
                elementsAdapter.updateElements(updatedElements)

                // Прокручиваем к последнему элементу
                if (updatedElements.isNotEmpty()) {
                    binding.elementsRecyclerView.scrollToPosition(updatedElements.size - 1)
                }
            }
        })
        dialog.show(parentFragmentManager, "AddExerciseDialog")
    }

    private fun editElement(element: WorkoutElement) {
        when (element) {
            is Exercise -> showEditExerciseDialog(element)
            is Block -> {
                // Вложенные блоки не поддерживаем
                Toast.makeText(requireContext(), "Вложенные блоки не поддерживаются", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditExerciseDialog(exercise: Exercise) {
        val dialog = EditExerciseDialog.newInstance(exercise)
        dialog.setListener(object : EditExerciseDialog.OnExerciseEditedListener {
            override fun onExerciseEdited(updatedExercise: Exercise) {
                val updatedElements = block.elements.map { element ->
                    if (element is Exercise && element.id == updatedExercise.id) {
                        updatedExercise
                    } else {
                        element
                    }
                }
                block = block.copy(elements = updatedElements)
                elementsAdapter.updateElements(updatedElements)
            }
        })
        dialog.show(parentFragmentManager, "EditExerciseDialog")
    }

    private fun showDeleteElementDialog(element: WorkoutElement) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление элемента")
            .setMessage("Вы уверены, что хотите удалить \"${element.name}\"?")
            .setPositiveButton("Удалить") { dialog, which ->
                val updatedElements = block.elements.toMutableList().apply {
                    removeAll { it.id == element.id }
                }
                block = block.copy(elements = updatedElements)
                elementsAdapter.updateElements(updatedElements)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}