package com.example.intervaltimer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(
    private val workouts: List<Workout>,
    private val onItemClick: (Workout) -> Unit,
    private val onItemLongClick: (Workout) -> Boolean
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.workoutNameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.workoutDescriptionTextView)

        fun bind(workout: Workout) {
            nameTextView.text = workout.name
            descriptionTextView.text = workout.description

            itemView.setOnClickListener {
                onItemClick(workout)
            }

            itemView.setOnLongClickListener {
                onItemLongClick(workout)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    override fun getItemCount() = workouts.size
}