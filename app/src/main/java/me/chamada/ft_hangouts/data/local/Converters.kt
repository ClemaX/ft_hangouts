package me.chamada.ft_hangouts.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun stringListFromJson(value: String): List<String> {
        return Json.decodeFromString<List<String>>(value)
    }

    @TypeConverter
    fun stringListToJson(value: List<String>?): String {
         return Json.encodeToString(value)
    }
}