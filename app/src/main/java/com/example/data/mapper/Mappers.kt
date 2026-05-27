package com.example.data.mapper

import com.example.data.local.NoteEntity
import com.example.data.local.NoteWithRelations
import com.example.data.local.NotebookEntity
import com.example.data.local.TagEntity
import com.example.data.local.Converters
import com.example.domain.model.Note
import com.example.domain.model.Notebook
import com.example.domain.model.Tag

fun NotebookEntity.toDomain(): Notebook {
    return Notebook(
        id = id,
        name = name,
        colorHex = colorHex,
        createdAt = createdAt
    )
}

fun Notebook.toEntity(): NotebookEntity {
    return NotebookEntity(
        id = id,
        name = name,
        colorHex = colorHex,
        createdAt = createdAt
    )
}

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        colorHex = colorHex
    )
}

fun Tag.toEntity(): TagEntity {
    return TagEntity(
        id = id,
        name = name,
        colorHex = colorHex
    )
}

fun NoteWithRelations.toDomain(): Note {
    val converters = Converters()
    return Note(
        id = note.id,
        title = note.title,
        content = converters.toBlockList(note.content),
        notebookId = note.notebookId,
        coverColor = note.coverColor,
        icon = note.icon,
        isPinned = note.isPinned,
        isFavorite = note.isFavorite,
        isDeleted = note.isDeleted,
        createdAt = note.createdAt,
        updatedAt = note.updatedAt,
        tags = tags.map { it.toDomain() },
        notebookName = notebook?.name
    )
}

fun Note.toEntity(): NoteEntity {
    val converters = Converters()
    return NoteEntity(
        id = id,
        title = title,
        content = converters.fromBlockList(content),
        notebookId = notebookId,
        coverColor = coverColor,
        icon = icon,
        isPinned = isPinned,
        isFavorite = isFavorite,
        isDeleted = isDeleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
