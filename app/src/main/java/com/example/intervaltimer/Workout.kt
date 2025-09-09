package com.example.intervaltimer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Workout(
    val id: Long,
    val name: String,
    val description: String = "",
    val elements: List<WorkoutElement> = emptyList()
) : Parcelable