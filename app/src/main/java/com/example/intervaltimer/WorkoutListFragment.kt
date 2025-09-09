package com.example.intervaltimer

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.intervaltimer.databinding.FragmentWorkoutListBinding

class WorkoutListFragment : Fragment() {
    private var _binding: FragmentWorkoutListBinding? = null
    private val binding get() = _binding!!
    private val workoutList get() = WorkoutManager.getAllWorkouts()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка RecyclerView
        binding.workoutRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.workoutRecyclerView.adapter = WorkoutAdapter(
            workouts = workoutList,
            onItemClick = { workout ->
                // Переход к редактору с выбранной тренировкой
                val bundle = Bundle().apply {
                    putParcelable("workout", workout)
                }
                findNavController().navigate(R.id.action_list_to_editor, bundle)
            },
            onItemLongClick = { workout ->
                // Показываем диалог подтверждения удаления
                showDeleteConfirmationDialog(workout)
                true
            }
        )

        binding.createWorkoutButton.setOnClickListener {
            // Создаем новую тренировку с уникальным ID
            val newWorkout = Workout(
                id = System.currentTimeMillis(),
                name = "Новая тренировка",
                description = "",
                elements = emptyList()
            )

            // Сохраняем новую тренировку
            WorkoutManager.saveWorkout(newWorkout)

            // Переходим к редактору с новой тренировкой
            val bundle = Bundle().apply {
                putParcelable("workout", newWorkout)
            }
            findNavController().navigate(R.id.action_list_to_editor, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()
        // При возвращении на экран обновляем список
        binding.workoutRecyclerView.adapter = WorkoutAdapter(
            workouts = WorkoutManager.getAllWorkouts(),
            onItemClick = { workout ->
                val bundle = Bundle().apply {
                    putParcelable("workout", workout)
                }
                findNavController().navigate(R.id.action_list_to_editor, bundle)
            },
            onItemLongClick = { workout ->
                showDeleteConfirmationDialog(workout)
                true
            }
        )
    }

    private fun showDeleteConfirmationDialog(workout: Workout) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление тренировки")
            .setMessage("Вы уверены, что хотите удалить тренировку \"${workout.name}\"?")
            .setPositiveButton("Удалить") { dialog, which ->
                WorkoutManager.deleteWorkout(workout)
                // Обновляем список
                binding.workoutRecyclerView.adapter?.notifyDataSetChanged()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}