package com.example.intervaltimer

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "elements",
    foreignKeys = [ForeignKey(
        entity = WorkoutEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("workoutId"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["workoutId"])] // Добавляем индекс
)
data class WorkoutElementEntity(
    @PrimaryKey val id: Long,
    val workoutId: Long,
    val type: String, // "exercise" или "block"
    val name: String,
    val duration: Long,
    val color: Int = -1,
    val elementType: String? = null, // Для упражнений: "PREP", "WORK", "REST"
    val rounds: Int? = null, // Для блоков
    val parentId: Long? = null // Для вложенных элементов (если элемент внутри блока)
)