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
        private val blockContentTextView: TextView = itemView.findViewById(R.id.blockContentTextView)

        fun bind(element: WorkoutElement) {
            // Сначала скрываем дополнительное текстовое поле
            blockContentTextView.visibility = View.GONE

            when (element) {
                is Exercise -> {
                    val typeText = when (element.type) {
                        ElementType.PREP -> "Подготовка"
                        ElementType.WORK -> "Работа"
                        ElementType.REST -> "Отдых"
                    }
                    nameTextView.text = element.name
                    detailsTextView.text = "$typeText - ${formatDuration(element.duration)}"
                    iconImageView.setImageResource(R.drawable.ic_exercise)
                }
                is Block -> {
                    nameTextView.text = element.name

                    // Основная информация о блоке
                    val elementsCount = element.elements.size
                    val totalDuration = element.duration
                    detailsTextView.text = "Блок: ${element.rounds} раундов, $elementsCount элементов, ${formatDuration(totalDuration)}"
                    iconImageView.setImageResource(R.drawable.ic_block)

                    // Дополнительная информация о содержимом блока
                    if (element.elements.isNotEmpty()) {
                        blockContentTextView.visibility = View.VISIBLE
                        val contentText = buildString {
                            append("Содержимое:\n")
                            element.elements.forEachIndexed { index, item ->
                                if (item is Exercise) {
                                    val typeChar = when (item.type) {
                                        ElementType.PREP -> "П"
                                        ElementType.WORK -> "Р"
                                        ElementType.REST -> "О"
                                    }
                                    append("${index + 1}. ${item.name} ($typeChar, ${formatDuration(item.duration)})\n")
                                }
                            }
                        }
                        blockContentTextView.text = contentText.trim()
                    }
                }
            }

            itemView.setOnClickListener {
                onItemClick(element)
            }

            itemView.setOnLongClickListener {
                onItemLongClick(element)
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