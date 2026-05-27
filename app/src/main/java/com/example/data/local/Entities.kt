package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "notebooks"
)
data class NotebookEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val createdAt: Long
)

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["notebookId"])]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String, // JSON block list
    val notebookId: String?,
    val coverColor: String,
    val icon: String,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val isDeleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String
)

@Entity(
    tableName = "note_tag_cross_ref",
    primaryKeys = ["noteId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["noteId"]),
        Index(value = ["tagId"])
    ]
)
data class NoteTagCrossRef(
    val noteId: String,
    val tagId: String
)
