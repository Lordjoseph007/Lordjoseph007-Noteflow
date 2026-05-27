package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.NoteBlock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromBlockList(blocks: List<NoteBlock>): String {
        return json.encodeToString(blocks)
    }

    @TypeConverter
    fun toBlockList(jsonStr: String): List<NoteBlock> {
        return try {
            json.decodeFromString(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
