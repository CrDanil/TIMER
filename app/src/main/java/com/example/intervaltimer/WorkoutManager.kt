package com.example.intervaltimer

object WorkoutManager {
    private val workouts = mutableListOf<Workout>()

    init {
        // Добавляем примеры тренировок при первом запуске
        if (workouts.isEmpty()) {
            workouts.addAll(getSampleWorkouts())
        }
    }

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

    fun getAllWorkouts(): List<Workout> {
        return workouts.toList()
    }

    fun saveWorkout(workout: Workout) {
        val existingIndex = workouts.indexOfFirst { it.id == workout.id }
        if (existingIndex != -1) {
            workouts[existingIndex] = workout
        } else {
            workouts.add(workout)
        }
    }

    fun deleteWorkout(workout: Workout) {
        workouts.removeAll { it.id == workout.id }
    }

    fun getWorkoutById(id: Long): Workout? {
        return workouts.find { it.id == id }
    }

    fun flattenWorkout(workout: Workout): List<TimerStep> {
        val stepList = mutableListOf<TimerStep>()
        for (element in workout.elements) {
            when (element) {
                is Exercise -> {
                    stepList.add(TimerStep(element.name, element.type, element.duration))
                }
                is Block -> {
                    for (round in 1..element.rounds) {
                        for (blockElement in element.elements) {
                            when (blockElement) {
                                is Exercise -> {
                                    stepList.add(TimerStep(
                                        "${element.name} (Раунд $round/${element.rounds}): ${blockElement.name}",
                                        blockElement.type,
                                        blockElement.duration
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
        val duration: Long // в миллисекундах
    )
}