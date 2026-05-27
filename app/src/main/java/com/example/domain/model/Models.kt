package com.example.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TextFormatting(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false
)

@Serializable
sealed class NoteBlock {
    @Serializable
    data class ParagraphBlock(val text: String, val formatting: TextFormatting = TextFormatting()) : NoteBlock()

    @Serializable
    data class HeadingBlock(val text: String, val level: Int) : NoteBlock() // 1, 2, or 3

    @Serializable
    data class BulletBlock(val text: String, val depth: Int) : NoteBlock()

    @Serializable
    data class NumberedBlock(val text: String, val index: Int) : NoteBlock()

    @Serializable
    data class TodoBlock(val text: String, val isChecked: Boolean) : NoteBlock()

    @Serializable
    data class DividerBlock(val id: String) : NoteBlock()

    @Serializable
    data class QuoteBlock(val text: String) : NoteBlock()

    @Serializable
    data class CodeBlock(val code: String, val language: String) : NoteBlock()
}

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: List<NoteBlock> = emptyList(),
    val notebookId: String? = null,
    val coverColor: String = "gradient_indigo",
    val icon: String = "📝",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tags: List<Tag> = emptyList(),
    val notebookName: String? = null
)

data class Notebook(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val colorHex: String = "#3D2C8D",
    val createdAt: Long = System.currentTimeMillis()
)

data class Tag(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val colorHex: String = "#FFD700"
)
