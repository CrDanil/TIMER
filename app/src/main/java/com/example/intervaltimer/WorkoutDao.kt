package com.example.intervaltimer

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkouts(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM elements WHERE workoutId = :workoutId AND parentId IS NULL")
    suspend fun getElementsForWorkout(workoutId: Long): List<WorkoutElementEntity>

    @Query("SELECT * FROM elements WHERE parentId = :parentId")
    suspend fun getElementsForBlock(parentId: Long): List<WorkoutElementEntity>

    @Insert
    suspend fun insertElement(element: WorkoutElementEntity)

    @Update
    suspend fun updateElement(element: WorkoutElementEntity)

    @Delete
    suspend fun deleteElement(element: WorkoutElementEntity)

    @Query("DELETE FROM elements WHERE workoutId = :workoutId")
    suspend fun deleteElementsForWorkout(workoutId: Long)
}