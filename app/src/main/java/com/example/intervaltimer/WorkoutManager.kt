package com.example.intervaltimer

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object WorkoutManager {
    private lateinit var database: AppDatabase
    private lateinit var workoutDao: WorkoutDao

    fun init(context: Context) {
        database = AppDatabase.getInstance(context)
        workoutDao = database.workoutDao()

        // Добавляем примеры тренировок при первом запуске, если база пуста
        CoroutineScope(Dispatchers.IO).launch {
            if (getAllWorkouts().isEmpty()) {
                getSampleWorkouts().forEach { saveWorkout(it) }
            }
        }
    }

    suspend fun getAllWorkouts(): List<Workout> {
        return withContext(Dispatchers.IO) {
            try {
                val workoutEntities = workoutDao.getAllWorkouts()
                workoutEntities.map { workoutEntity ->
                    val elements = loadWorkoutElements(workoutEntity.id)
                    Workout(
                        id = workoutEntity.id,
                        name = workoutEntity.name,
                        description = workoutEntity.description,
                        elements = elements
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun loadWorkoutElements(workoutId: Long): List<WorkoutElement> {
        return withContext(Dispatchers.IO) {
            try {
                val elementEntities = workoutDao.getElementsForWorkout(workoutId)
                elementEntities.map { entity ->
                    when (entity.type) {
                        "exercise" -> Exercise(
                            id = entity.id,
                            name = entity.name,
                            type = ElementType.valueOf(entity.elementType!!),
                            duration = entity.duration,
                            color = entity.color
                        )
                        "block" -> {
                            val blockElements = loadBlockElements(entity.id)
                            Block(
                                id = entity.id,
                                name = entity.name,
                                rounds = entity.rounds!!,
                                elements = blockElements,
                                duration = entity.duration
                            )
                        }
                        else -> throw IllegalArgumentException("Unknown element type: ${entity.type}")
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun loadBlockElements(blockId: Long): List<WorkoutElement> {
        return withContext(Dispatchers.IO) {
            try {
                val elementEntities = workoutDao.getElementsForBlock(blockId)
                elementEntities.map { entity ->
                    when (entity.type) {
                        "exercise" -> Exercise(
                            id = entity.id,
                            name = entity.name,
                            type = ElementType.valueOf(entity.elementType!!),
                            duration = entity.duration,
                            color = entity.color
                        )
                        else -> throw IllegalArgumentException("Nested blocks are not supported")
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveWorkout(workout: Workout) {
        withContext(Dispatchers.IO) {
            try {
                // Сохраняем или обновляем тренировку
                val workoutEntity = WorkoutEntity(workout.id, workout.name, workout.description)
                if (workoutDao.getWorkoutById(workout.id) == null) {
                    workoutDao.insertWorkout(workoutEntity)
                } else {
                    workoutDao.updateWorkout(workoutEntity)
                }

                // Удаляем старые элементы
                workoutDao.deleteElementsForWorkout(workout.id)

                // Сохраняем элементы
                saveWorkoutElements(workout.id, workout.elements, null)
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

    private suspend fun saveWorkoutElements(workoutId: Long, elements: List<WorkoutElement>, parentId: Long?) {
        withContext(Dispatchers.IO) {
            elements.forEach { element ->
                when (element) {
                    is Exercise -> {
                        workoutDao.insertElement(
                            WorkoutElementEntity(
                                id = element.id,
                                workoutId = workoutId,
                                type = "exercise",
                                name = element.name,
                                duration = element.duration,
                                color = element.color,
                                elementType = element.type.name,
                                parentId = parentId
                            )
                        )
                    }
                    is Block -> {
                        workoutDao.insertElement(
                            WorkoutElementEntity(
                                id = element.id,
                                workoutId = workoutId,
                                type = "block",
                                name = element.name,
                                duration = element.duration,
                                rounds = element.rounds,
                                parentId = parentId
                            )
                        )
                        // Сохраняем элементы блока
                        saveWorkoutElements(workoutId, element.elements, element.id)
                    }
                }
            }
        }
    }

    suspend fun deleteWorkout(workout: Workout) {
        withContext(Dispatchers.IO) {
            try {
                workoutDao.deleteElementsForWorkout(workout.id)
                workoutDao.deleteWorkout(WorkoutEntity(workout.id, workout.name, workout.description))
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

    suspend fun getWorkoutById(id: Long): Workout? {
        return withContext(Dispatchers.IO) {
            try {
                val workoutEntity = workoutDao.getWorkoutById(id) ?: return@withContext null
                val elements = loadWorkoutElements(workoutEntity.id)
                Workout(
                    id = workoutEntity.id,
                    name = workoutEntity.name,
                    description = workoutEntity.description,
                    elements = elements
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    // Остальные методы (getSampleWorkouts, flattenWorkout) остаются без изменений
    private fun getSampleWorkouts(): List<Workout> {
        return listOf(
            Workout(
                id = 1,
                name = "Утренняя зарядка",
                description = "Легкая разминка на 10 минут",
                elements = listOf(
                    Exercise(101, "Разминка", ElementType.PREP, 60000),
                    Exercise(102, "Бег на месте", ElementType.WORK, 120000),
                    Exercise(103, "Отдых", ElementType.REST, 30000)
                )
            ),
            Workout(
                id = 2,
                name = "Интервальный бег",
                description = "Бег с интервалами 30/30",
                elements = listOf(
                    Exercise(201, "Разогрев", ElementType.PREP, 120000),
                    Exercise(202, "Спринт", ElementType.WORK, 30000),
                    Exercise(203, "Ходьба", ElementType.REST, 30000)
                )
            )
        )
    }

    fun flattenWorkout(workout: Workout): List<TimerStep> {
        val stepList = mutableListOf<TimerStep>()
        for (element in workout.elements) {
            when (element) {
                is Exercise -> {
                    stepList.add(TimerStep(element.name, element.type, element.duration, element.color))
                }
                is Block -> {
                    for (round in 1..element.rounds) {
                        for (blockElement in element.elements) {
                            when (blockElement) {
                                is Exercise -> {
                                    stepList.add(TimerStep(
                                        "${element.name} ($round/${element.rounds}): ${blockElement.name}",
                                        blockElement.type,
                                        blockElement.duration,
                                        blockElement.color
                                    ))
                                }
                                is Block -> {
                                    // Вложенные блоки не поддерживаем
                                }
                            }
                        }
                    }
                }
            }
        }
        return stepList
    }

    data class TimerStep(
        val name: String,
        val type: ElementType,
        val duration: Long,
        val color: Int // Добавляем цвет
    )
}