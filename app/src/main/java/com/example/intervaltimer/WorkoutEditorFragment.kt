package com.example.intervaltimer

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.intervaltimer.databinding.FragmentWorkoutEditorBinding

class WorkoutEditorFragment : Fragment() {
    private var _binding: FragmentWorkoutEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentWorkout: Workout
    private lateinit var elementsAdapter: WorkoutElementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Получаем переданную тренировку или создаем новую
        val workout = arguments?.getParcelable<Workout>("workout")
        currentWorkout = workout ?: Workout(
            id = System.currentTimeMillis(),
            name = "Новая тренировка",
            description = "",
            elements = emptyList()
        )

        // Заполняем поля
        binding.workoutNameEditText.setText(currentWorkout.name)
        binding.workoutDescriptionEditText.setText(currentWorkout.description)

        // Настраиваем список элементов
        elementsAdapter = WorkoutElementAdapter(
            elements = currentWorkout.elements,
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

        // Обработчики кнопок
        binding.addElementButton.setOnClickListener {
            showAddExerciseDialog()
        }

        binding.addBlockButton.setOnClickListener {
            showAddBlockDialog()
        }

        binding.saveWorkoutButton.setOnClickListener {
            saveWorkout()
            requireActivity().onBackPressed()
        }

        binding.startWorkoutButton.setOnClickListener {
            // Сначала сохраняем тренировку
            saveWorkout()

            // Затем запускаем таймер
            val bundle = Bundle().apply {
                putParcelable("workout", currentWorkout)
            }
            findNavController().navigate(R.id.action_editor_to_timer, bundle)
        }
    }


    private fun saveWorkout() {
        val name = binding.workoutNameEditText.text.toString()
        val description = binding.workoutDescriptionEditText.text.toString()

        // Вычисляем общую длительность тренировки
        val totalDuration = calculateTotalDuration(currentWorkout.elements)

        currentWorkout = currentWorkout.copy(
            name = name,
            description = description,
            // duration можно добавить в модель Workout, если нужно
        )

        // Сохраняем в менеджер
        WorkoutManager.saveWorkout(currentWorkout)

        // Показываем общую длительность
        Toast.makeText(requireContext(), "Тренировка сохранена. Общая длительность: ${formatDuration(totalDuration)}", Toast.LENGTH_SHORT).show()
    }

    private fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun calculateTotalDuration(elements: List<WorkoutElement>): Long {
        var totalDuration = 0L
        for (element in elements) {
            when (element) {
                is Exercise -> totalDuration += element.duration
                is Block -> totalDuration += element.duration
            }
        }
        return totalDuration
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showAddExerciseDialog() {
        val dialog = AddExerciseDialog()
        dialog.setListener(object : AddExerciseDialog.OnExerciseAddedListener {
            override fun onExerciseAdded(exercise: Exercise) {
                val updatedElements = currentWorkout.elements.toMutableList().apply {
                    add(exercise)
                }
                currentWorkout = currentWorkout.copy(elements = updatedElements)
                updateElementsList() // Теперь это просто обновляет существующий адаптер
            }
        })
        dialog.show(parentFragmentManager, "AddExerciseDialog")
    }

    private fun showAddBlockDialog() {
        val dialog = AddBlockDialog()
        dialog.setListener(object : AddBlockDialog.OnBlockAddedListener {
            override fun onBlockAdded(block: Block) {
                val updatedElements = currentWorkout.elements.toMutableList().apply {
                    add(block)
                }
                currentWorkout = currentWorkout.copy(elements = updatedElements)
                updateElementsList()
            }
        })
        dialog.show(parentFragmentManager, "AddBlockDialog")
    }

    private fun editElement(element: WorkoutElement) {
        when (element) {
            is Exercise -> showEditExerciseDialog(element)
            is Block -> showEditBlockDialog(element)
        }
    }

    private fun showEditExerciseDialog(exercise: Exercise) {
        val dialog = EditExerciseDialog.newInstance(exercise)
        dialog.setListener(object : EditExerciseDialog.OnExerciseEditedListener {
            override fun onExerciseEdited(updatedExercise: Exercise) {
                val updatedElements = currentWorkout.elements.map { element ->
                    if (element is Exercise && element.id == updatedExercise.id) {
                        updatedExercise
                    } else {
                        element
                    }
                }
                currentWorkout = currentWorkout.copy(elements = updatedElements)
                updateElementsList() // Обновляем существующий адаптер
            }
        })
        dialog.show(parentFragmentManager, "EditExerciseDialog")
    }

    private fun showEditBlockDialog(block: Block) {
        // TODO: Реализовать редактирование блоков
        android.widget.Toast.makeText(requireContext(), "Редактирование блоков будет реализовано позже", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteElementDialog(element: WorkoutElement) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление элемента")
            .setMessage("Вы уверены, что хотите удалить \"${element.name}\"?")
            .setPositiveButton("Удалить") { dialog, which ->
                val updatedElements = currentWorkout.elements.toMutableList().apply {
                    removeAll { it.id == element.id }
                }
                currentWorkout = currentWorkout.copy(elements = updatedElements)
                updateElementsList() // Обновляем существующий адаптер
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    private fun updateElementsList() {
        elementsAdapter.updateElements(currentWorkout.elements)

        // Прокручиваем к последнему элементу, если список не пустой
        if (currentWorkout.elements.isNotEmpty()) {
            binding.elementsRecyclerView.scrollToPosition(currentWorkout.elements.size - 1)
        }
    }

}

