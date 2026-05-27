package com.example

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.NoteRepositoryImpl
import com.example.data.repository.NotebookRepositoryImpl
import com.example.data.repository.TagRepositoryImpl
import com.example.domain.repository.NoteRepository
import com.example.domain.repository.NotebookRepository
import com.example.domain.repository.TagRepository
import com.example.domain.usecase.*

private val Context.dataStore by preferencesDataStore(name = "noteflow_settings")

class AppContainer(private val context: Context) {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "noteflow_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl(database.noteDao())
    }

    val notebookRepository: NotebookRepository by lazy {
        NotebookRepositoryImpl(database.notebookDao())
    }

    val tagRepository: TagRepository by lazy {
        TagRepositoryImpl(database.tagDao())
    }

    // Use cases
    val getActiveNotesUseCase by lazy { GetActiveNotesUseCase(noteRepository) }
    val getNotesByNotebookUseCase by lazy { GetNotesByNotebookUseCase(noteRepository) }
    val getNotesByTagUseCase by lazy { GetNotesByTagUseCase(noteRepository) }
    val searchNotesUseCase by lazy { SearchNotesUseCase(noteRepository) }
    val getTrashedNotesUseCase by lazy { GetTrashedNotesUseCase(noteRepository) }
    val getFavoriteNotesUseCase by lazy { GetFavoriteNotesUseCase(noteRepository) }
    val getNoteByIdUseCase by lazy { GetNoteByIdUseCase(noteRepository) }
    val saveNoteUseCase by lazy { SaveNoteUseCase(noteRepository) }
    val markNoteAsTrashUseCase by lazy { MarkNoteAsTrashUseCase(noteRepository) }
    val deleteNotePermanentlyUseCase by lazy { DeleteNotePermanentlyUseCase(noteRepository) }
    
    val settingsManager by lazy { com.example.data.local.SettingsManager(dataStore) }
    
    val dataStore get() = context.dataStore
}

class NoteFlowApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
