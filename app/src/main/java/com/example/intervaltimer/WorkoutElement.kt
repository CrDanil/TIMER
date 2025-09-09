package com.example.intervaltimer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class WorkoutElement : Parcelable {
    abstract val id: Long
    abstract val name: String
    abstract val duration: Long // в миллисекундах
}

@Parcelize
data class Exercise(
    override val id: Long,
    override val name: String,
    val type: ElementType,
    override val duration: Long
) : WorkoutElement()

@Parcelize
data class Block(
    override val id: Long,
    override val name: String,
    val rounds: Int,
    val elements: List<WorkoutElement>,
    override val duration: Long // общая длительность блока
) : WorkoutElement()

enum class ElementType {
    PREP, WORK, REST
}