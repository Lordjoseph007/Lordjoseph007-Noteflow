package com.example.domain.usecase

import com.example.domain.model.Note
import com.example.domain.model.Notebook
import com.example.domain.model.Tag
import com.example.domain.repository.NoteRepository
import com.example.domain.repository.NotebookRepository
import com.example.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetActiveNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.getAllActiveNotes()
}

class GetNotesByNotebookUseCase(private val repository: NoteRepository) {
    operator fun invoke(notebookId: String): Flow<List<Note>> = repository.getNotesByNotebook(notebookId)
}

class GetNotesByTagUseCase(private val repository: NoteRepository) {
    operator fun invoke(tagId: String): Flow<List<Note>> = repository.getNotesByTag(tagId)
}

class SearchNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(query: String): Flow<List<Note>> {
        return if (query.isBlank()) {
            repository.getAllActiveNotes()
        } else {
            repository.searchNotes(query)
        }
    }
}

class GetTrashedNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.getTrashedNotes()
}

class GetFavoriteNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.getFavoriteNotes()
}

class GetNoteByIdUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(noteId: String): Note? = repository.getNoteById(noteId)
}

class SaveNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note) {
        if (note.title.isBlank() && note.content.isEmpty()) return // Don't save empty notes
        repository.saveNote(note.copy(updatedAt = System.currentTimeMillis()))
    }
}

class MarkNoteAsTrashUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(noteId: String, isDeleted: Boolean) {
        repository.markAsTrash(noteId, isDeleted)
    }
}

class DeleteNotePermanentlyUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note) = repository.deleteNotePermanently(note)
}
