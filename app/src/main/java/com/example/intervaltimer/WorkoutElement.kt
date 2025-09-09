package com.example.intervaltimer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class WorkoutElement : Parcelable {
    abstract val id: Long
    abstract val name: String
    abstract val duration: Long
    abstract val color: Int // Добавляем цвет
}

@Parcelize
data class Exercise(
    override val id: Long,
    override val name: String,
    val type: ElementType,
    override val duration: Long,
    override val color: Int = -1 // -1 означает цвет по умолчанию
) : WorkoutElement()

@Parcelize
data class Block(
    override val id: Long,
    override val name: String,
    val rounds: Int,
    val elements: List<WorkoutElement>,
    override val duration: Long
) : WorkoutElement() {
    // Вычисляем длительность на основе вложенных элементов
    override val color: Int = -1 // Блоки не имеют своего цвета
}

enum class ElementType {
    PREP, WORK, REST
}