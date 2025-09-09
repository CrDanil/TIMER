package com.example.intervaltimer

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.intervaltimer.databinding.FragmentWorkoutListBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutListFragment : Fragment() {
    private var _binding: FragmentWorkoutListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadWorkouts()

        binding.createWorkoutButton.setOnClickListener {
            val newWorkout = Workout(
                id = System.currentTimeMillis(),
                name = "Новая тренировка",
                description = "",
                elements = emptyList()
            )

            lifecycleScope.launch {
                WorkoutManager.saveWorkout(newWorkout)
                val bundle = Bundle().apply {
                    putParcelable("workout", newWorkout)
                }
                findNavController().navigate(R.id.action_list_to_editor, bundle)
            }
        }
    }

    private fun loadWorkouts() {
        lifecycleScope.launch {
            val workouts = withContext(kotlinx.coroutines.Dispatchers.IO) {
                WorkoutManager.getAllWorkouts()
            }

            binding.workoutRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.workoutRecyclerView.adapter = WorkoutAdapter(
                workouts = workouts,
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
    }

    private fun showDeleteConfirmationDialog(workout: Workout) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление тренировки")
            .setMessage("Вы уверены, что хотите удалить тренировку \"${workout.name}\"?")
            .setPositiveButton("Удалить") { dialog, which ->
                lifecycleScope.launch {
                    WorkoutManager.deleteWorkout(workout)
                    loadWorkouts()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadWorkouts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}