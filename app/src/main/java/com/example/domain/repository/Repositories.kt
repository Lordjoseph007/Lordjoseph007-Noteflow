package com.example.domain.repository

import com.example.domain.model.Note
import com.example.domain.model.Notebook
import com.example.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllActiveNotes(): Flow<List<Note>>
    fun getNotesByNotebook(notebookId: String): Flow<List<Note>>
    fun getNotesByTag(tagId: String): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    fun getTrashedNotes(): Flow<List<Note>>
    fun getFavoriteNotes(): Flow<List<Note>>
    suspend fun getNoteById(noteId: String): Note?
    suspend fun saveNote(note: Note)
    suspend fun markAsTrash(noteId: String, isDeleted: Boolean)
    suspend fun deleteNotePermanently(note: Note)
    suspend fun addTagToNote(noteId: String, tagId: String)
    suspend fun removeTagFromNote(noteId: String, tagId: String)
}

interface NotebookRepository {
    fun getAllNotebooks(): Flow<List<Notebook>>
    suspend fun getNotebookById(id: String): Notebook?
    suspend fun saveNotebook(notebook: Notebook)
    suspend fun deleteNotebook(notebook: Notebook)
}

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    suspend fun getTagById(id: String): Tag?
    suspend fun saveTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
}
