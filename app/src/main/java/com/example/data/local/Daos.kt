package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Relation class to fetch Note along with its Notebook and Tags
data class NoteWithRelations(
    @Embedded val note: NoteEntity,
    
    @Relation(
        parentColumn = "notebookId",
        entityColumn = "id"
    )
    val notebook: NotebookEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

@Dao
interface NoteDao {
    @Transaction
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveNotes(): Flow<List<NoteWithRelations>>

    @Transaction
    @Query("SELECT * FROM notes WHERE notebookId = :notebookId AND isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByNotebook(notebookId: String): Flow<List<NoteWithRelations>>

    @Transaction
    @Query("SELECT n.* FROM notes n INNER JOIN note_tag_cross_ref ncr ON n.id = ncr.noteId WHERE ncr.tagId = :tagId AND n.isDeleted = 0 ORDER BY n.isPinned DESC, n.updatedAt DESC")
    fun getNotesByTag(tagId: String): Flow<List<NoteWithRelations>>

    @Transaction
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND (title LIKE :query OR content LIKE :query) ORDER BY isPinned DESC, updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteWithRelations>>

    @Transaction
    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getTrashedNotes(): Flow<List<NoteWithRelations>>

    @Transaction
    @Query("SELECT * FROM notes WHERE isFavorite = 1 AND isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getFavoriteNotes(): Flow<List<NoteWithRelations>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    suspend fun getNoteWithRelationsById(noteId: String): NoteWithRelations?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    // Note - Tag relations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: NoteTagCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: NoteTagCrossRef)

    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun deleteCrossRefsForNote(noteId: String)
}

@Dao
interface NotebookDao {
    @Query("SELECT * FROM notebooks ORDER BY createdAt DESC")
    fun getAllNotebooks(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks WHERE id = :id LIMIT 1")
    suspend fun getNotebookById(id: String): NotebookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notebook: NotebookEntity)

    @Update
    suspend fun update(notebook: NotebookEntity)

    @Delete
    suspend fun delete(notebook: NotebookEntity)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id LIMIT 1")
    suspend fun getTagById(id: String): TagEntity?

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity)

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)
}
