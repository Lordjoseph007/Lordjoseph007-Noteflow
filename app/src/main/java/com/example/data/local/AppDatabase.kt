package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        NoteEntity::class,
        NotebookEntity::class,
        TagEntity::class,
        NoteTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun notebookDao(): NotebookDao
    abstract fun tagDao(): TagDao
}
