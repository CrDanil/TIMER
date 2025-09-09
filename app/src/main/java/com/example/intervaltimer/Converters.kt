package com.example.intervaltimer

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromWorkoutElementList(elements: List<WorkoutElement>): String {
        return gson.toJson(elements)
    }

    @TypeConverter
    fun toWorkoutElementList(data: String): List<WorkoutElement> {
        val listType = object : TypeToken<List<WorkoutElement>>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun fromElementType(elementType: ElementType): String {
        return elementType.name
    }

    @TypeConverter
    fun toElementType(data: String): ElementType {
        return ElementType.valueOf(data)
    }
}