package com.example.data.repository

import com.example.data.local.*
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.Note
import com.example.domain.model.Notebook
import com.example.domain.model.Tag
import com.example.domain.repository.NoteRepository
import com.example.domain.repository.NotebookRepository
import com.example.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllActiveNotes(): Flow<List<Note>> {
        return noteDao.getAllActiveNotes().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getNotesByNotebook(notebookId: String): Flow<List<Note>> {
        return noteDao.getNotesByNotebook(notebookId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getNotesByTag(tagId: String): Flow<List<Note>> {
        return noteDao.getNotesByTag(tagId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        val searchPattern = "%$query%"
        return noteDao.searchNotes(searchPattern).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTrashedNotes(): Flow<List<Note>> {
        return noteDao.getTrashedNotes().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFavoriteNotes(): Flow<List<Note>> {
        return noteDao.getFavoriteNotes().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getNoteById(noteId: String): Note? {
        return noteDao.getNoteWithRelationsById(noteId)?.toDomain()
    }

    override suspend fun saveNote(note: Note) {
        val entity = note.toEntity()
        noteDao.insert(entity)
        
        // Sync tags
        noteDao.deleteCrossRefsForNote(note.id)
        note.tags.forEach { tag ->
            noteDao.insertCrossRef(NoteTagCrossRef(note.id, tag.id))
        }
    }

    override suspend fun markAsTrash(noteId: String, isDeleted: Boolean) {
        val noteWithRel = noteDao.getNoteWithRelationsById(noteId)
        if (noteWithRel != null) {
            val updated = noteWithRel.note.copy(
                isDeleted = isDeleted,
                updatedAt = System.currentTimeMillis()
            )
            noteDao.update(updated)
        }
    }

    override suspend fun deleteNotePermanently(note: Note) {
        val entity = note.toEntity()
        noteDao.delete(entity)
        noteDao.deleteCrossRefsForNote(note.id)
    }

    override suspend fun addTagToNote(noteId: String, tagId: String) {
        noteDao.insertCrossRef(NoteTagCrossRef(noteId, tagId))
    }

    override suspend fun removeTagFromNote(noteId: String, tagId: String) {
        noteDao.deleteCrossRef(NoteTagCrossRef(noteId, tagId))
    }
}

class NotebookRepositoryImpl(
    private val notebookDao: NotebookDao
) : NotebookRepository {

    override fun getAllNotebooks(): Flow<List<Notebook>> {
        return notebookDao.getAllNotebooks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getNotebookById(id: String): Notebook? {
        return notebookDao.getNotebookById(id)?.toDomain()
    }

    override suspend fun saveNotebook(notebook: Notebook) {
        notebookDao.insert(notebook.toEntity())
    }

    override suspend fun deleteNotebook(notebook: Notebook) {
        notebookDao.delete(notebook.toEntity())
    }
}

class TagRepositoryImpl(
    private val tagDao: TagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getTagById(id: String): Tag? {
        return tagDao.getTagById(id)?.toDomain()
    }

    override suspend fun saveTag(tag: Tag) {
        tagDao.insert(tag.toEntity())
    }

    override suspend fun deleteTag(tag: Tag) {
        tagDao.delete(tag.toEntity())
    }
}
