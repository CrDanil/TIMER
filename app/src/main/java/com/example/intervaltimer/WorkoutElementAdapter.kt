package com.example.intervaltimer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutElementAdapter(
    private var elements: List<WorkoutElement>,
    private val onItemClick: (WorkoutElement) -> Unit,
    private val onItemLongClick: (WorkoutElement) -> Boolean
) : RecyclerView.Adapter<WorkoutElementAdapter.ElementViewHolder>() {

    inner class ElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.elementNameTextView)
        private val detailsTextView: TextView = itemView.findViewById(R.id.elementDetailsTextView)
        private val iconImageView: ImageView = itemView.findViewById(R.id.elementIconImageView)

        fun bind(element: WorkoutElement) {
            nameTextView.text = element.name

            when (element) {
                is Exercise -> {
                    val typeText = when (element.type) {
                        ElementType.PREP -> "Подготовка"
                        ElementType.WORK -> "Работа"
                        ElementType.REST -> "Отдых"
                    }
                    detailsTextView.text = "$typeText - ${formatDuration(element.duration)}"
                    iconImageView.setImageResource(R.drawable.ic_exercise)
                }
                is Block -> {
                    val elementsCount = element.elements.size
                    detailsTextView.text = "Блок: ${element.rounds} раундов, $elementsCount элементов"
                    iconImageView.setImageResource(R.drawable.ic_block)
                }
            }

            itemView.setOnClickListener {
                onItemClick(element)
            }

            itemView.setOnLongClickListener {
                onItemLongClick(element)
                true // Возвращаем true, чтобы показать, что событие обработано
            }
        }

        private fun formatDuration(milliseconds: Long): String {
            val totalSeconds = milliseconds / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_element, parent, false)
        return ElementViewHolder(view)
    }

    override fun onBindViewHolder(holder: ElementViewHolder, position: Int) {
        holder.bind(elements[position])
    }

    fun updateElements(newElements: List<WorkoutElement>) {
        elements = newElements
        notifyDataSetChanged()
    }

    override fun getItemCount() = elements.size
}