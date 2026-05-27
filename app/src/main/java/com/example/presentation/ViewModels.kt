package com.example.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ThemeMode
import com.example.data.local.FontSize
import com.example.data.local.SettingsManager
import com.example.domain.model.*
import com.example.domain.repository.NoteRepository
import com.example.domain.repository.NotebookRepository
import com.example.domain.repository.TagRepository
import com.example.domain.usecase.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// --------------------------------------------------------
// ViewModel Factory
// --------------------------------------------------------
class ViewModelFactory(
    private val context: android.content.Context,
    private val appContainer: com.example.AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    getActiveNotesUseCase = appContainer.getActiveNotesUseCase,
                    searchNotesUseCase = appContainer.searchNotesUseCase,
                    getNotesByNotebookUseCase = appContainer.getNotesByNotebookUseCase,
                    getNotesByTagUseCase = appContainer.getNotesByTagUseCase,
                    getFavoriteNotesUseCase = appContainer.getFavoriteNotesUseCase,
                    notebookRepository = appContainer.notebookRepository,
                    tagRepository = appContainer.tagRepository,
                    noteRepository = appContainer.noteRepository,
                    settingsManager = appContainer.settingsManager
                ) as T
            }
            modelClass.isAssignableFrom(EditorViewModel::class.java) -> {
                EditorViewModel(
                    getNoteByIdUseCase = appContainer.getNoteByIdUseCase,
                    saveNoteUseCase = appContainer.saveNoteUseCase,
                    notebookRepository = appContainer.notebookRepository,
                    tagRepository = appContainer.tagRepository,
                    noteRepository = appContainer.noteRepository
                ) as T
            }
            modelClass.isAssignableFrom(NotebooksViewModel::class.java) -> {
                NotebooksViewModel(
                    notebookRepository = appContainer.notebookRepository
                ) as T
            }
            modelClass.isAssignableFrom(TagsViewModel::class.java) -> {
                TagsViewModel(
                    tagRepository = appContainer.tagRepository
                ) as T
            }
            modelClass.isAssignableFrom(TrashViewModel::class.java) -> {
                TrashViewModel(
                    getTrashedNotesUseCase = appContainer.getTrashedNotesUseCase,
                    noteRepository = appContainer.noteRepository,
                    deleteNotePermanentlyUseCase = appContainer.deleteNotePermanentlyUseCase
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    settingsManager = appContainer.settingsManager,
                    context = context
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

// --------------------------------------------------------
// 1. Home / Dashboard ViewModel
// --------------------------------------------------------
class HomeViewModel(
    private val getActiveNotesUseCase: GetActiveNotesUseCase,
    private val searchNotesUseCase: SearchNotesUseCase,
    private val getNotesByNotebookUseCase: GetNotesByNotebookUseCase,
    private val getNotesByTagUseCase: GetNotesByTagUseCase,
    private val getFavoriteNotesUseCase: GetFavoriteNotesUseCase,
    private val notebookRepository: NotebookRepository,
    private val tagRepository: TagRepository,
    private val noteRepository: NoteRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView = _isGridView.asStateFlow()

    private val _selectedNotebookId = MutableStateFlow<String?>(null)
    val selectedNotebookId = _selectedNotebookId.asStateFlow()

    private val _selectedTagId = MutableStateFlow<String?>(null)
    val selectedTagId = _selectedTagId.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites = _showOnlyFavorites.asStateFlow()

    // Welcomes user
    val userName = settingsManager.userNameFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "Creator"
    )

    val greeting = userName.map { name ->
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good morning, $name"
            in 12..16 -> "Good afternoon, $name"
            else -> "Good evening, $name"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Welcome back")

    // Folders/Notebooks
    val notebooks = notebookRepository.getAllNotebooks().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Tags
    val tags = tagRepository.getAllTags().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Dynamic list of active notes
    val notes = combine(
        _searchQuery,
        _selectedNotebookId,
        _selectedTagId,
        _showOnlyFavorites
    ) { query, notebookId, tagId, favoritesOnly ->
        Triple(query, notebookId, Pair(tagId, favoritesOnly))
    }.flatMapLatest { triple ->
        val query = triple.first
        val notebookId = triple.second
        val tagId = triple.third.first
        val favoritesOnly = triple.third.second
        
        when {
            favoritesOnly -> getFavoriteNotesUseCase()
            notebookId != null -> getNotesByNotebookUseCase(notebookId)
            tagId != null -> getNotesByTagUseCase(tagId)
            else -> searchNotesUseCase(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun selectNotebook(notebookId: String?) {
        _selectedNotebookId.value = notebookId
        _selectedTagId.value = null
        _showOnlyFavorites.value = false
    }

    fun selectTag(tagId: String?) {
        _selectedTagId.value = tagId
        _selectedNotebookId.value = null
        _showOnlyFavorites.value = false
    }

    fun toggleFavoritesFilter() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
        _selectedNotebookId.value = null
        _selectedTagId.value = null
    }

    fun clearFilters() {
        _selectedNotebookId.value = null
        _selectedTagId.value = null
        _showOnlyFavorites.value = false
        _searchQuery.value = ""
    }

    fun pinNote(note: Note, isPinned: Boolean) {
        viewModelScope.launch {
            noteRepository.saveNote(note.copy(isPinned = isPinned))
        }
    }

    fun favoriteNote(note: Note, isFavorite: Boolean) {
        viewModelScope.launch {
            noteRepository.saveNote(note.copy(isFavorite = isFavorite))
        }
    }

    fun deleteNoteToTrash(noteId: String) {
        viewModelScope.launch {
            noteRepository.markAsTrash(noteId, true)
        }
    }

    // Fast seed helper in case user has empty list
    fun createSampleNotes() {
        viewModelScope.launch {
            // Check if databases are empty
            if (notebooks.value.isEmpty()) {
                val personal = Notebook(name = "Personal", colorHex = "#3D2C8D")
                val work = Notebook(name = "Work Projects", colorHex = "#264653")
                notebookRepository.saveNotebook(personal)
                notebookRepository.saveNotebook(work)

                val ideaTag = Tag(name = "Idea", colorHex = "#FFD700")
                val importantTag = Tag(name = "Important", colorHex = "#E76F51")
                tagRepository.saveTag(ideaTag)
                tagRepository.saveTag(importantTag)

                val firstNote = Note(
                    title = "Getting Started in NoteFlow 🚀",
                    content = listOf(
                        NoteBlock.HeadingBlock("Welcome to NoteFlow!", 1),
                        NoteBlock.ParagraphBlock("NoteFlow is a block-based note editor inspired by Notion. Every line is a paragraph, list item, checkbox, quote, or block code element."),
                        NoteBlock.QuoteBlock("Simple, visual, and entirely offline. Pin your notes, categorize them in folders/notebooks, and keep thoughts flowing."),
                        NoteBlock.DividerBlock("div_1"),
                        NoteBlock.HeadingBlock("Quick Checklist", 2),
                        NoteBlock.TodoBlock("Create your first color-coded Notebook folder", true),
                        NoteBlock.TodoBlock("Add tags to quickly filter notes", false),
                        NoteBlock.TodoBlock("Try out the dark mode in settings", false),
                        NoteBlock.HeadingBlock("Snippet Example", 3),
                        NoteBlock.CodeBlock("fun main() {\n   println(\"Happy Writing!\")\n}", "kotlin")
                    ),
                    notebookId = personal.id,
                    coverColor = "gradient_indigo",
                    icon = "🚀",
                    tags = listOf(ideaTag)
                )
                noteRepository.saveNote(firstNote)
            }
        }
    }
}

// --------------------------------------------------------
// 2. Note Editor ViewModel
// --------------------------------------------------------
class EditorViewModel(
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val notebookRepository: NotebookRepository,
    private val tagRepository: TagRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _noteId = mutableStateOf<String?>(null)
    val noteId: State<String?> = _noteId

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    private val _blocks = mutableStateOf<List<NoteBlock>>(listOf(NoteBlock.ParagraphBlock("")))
    val blocks: State<List<NoteBlock>> = _blocks

    private val _selectedNotebookId = mutableStateOf<String?>(null)
    val selectedNotebookId: State<String?> = _selectedNotebookId

    private val _coverColor = mutableStateOf("gradient_indigo")
    val coverColor: State<String> = _coverColor

    private val _iconSymbol = mutableStateOf("📝")
    val iconSymbol: State<String> = _iconSymbol

    private val _isPinned = mutableStateOf(false)
    val isPinned: State<Boolean> = _isPinned

    private val _isFavorite = mutableStateOf(false)
    val isFavorite: State<Boolean> = _isFavorite

    private val _isSaving = mutableStateOf(false)
    val isSaving: State<Boolean> = _isSaving

    private val _noteTags = mutableStateOf<List<Tag>>(emptyList())
    val noteTags: State<List<Tag>> = _noteTags

    val notebooks = notebookRepository.getAllNotebooks().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allTags = tagRepository.getAllTags().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private var hasChanged = false
    private var initialLoadCompleted = false

    init {
        // Auto-save loop - matches auto-save every 5 seconds requirement
        viewModelScope.launch {
            while (true) {
                delay(5000)
                if (hasChanged && initialLoadCompleted) {
                    performSave()
                }
            }
        }
    }

    fun loadNote(id: String?) {
        if (id == null) {
            // New note
            _noteId.value = UUID.randomUUID().toString()
            _title.value = ""
            _blocks.value = listOf(NoteBlock.ParagraphBlock(""))
            _selectedNotebookId.value = null
            _coverColor.value = "gradient_indigo"
            _iconSymbol.value = "📝"
            _isPinned.value = false
            _isFavorite.value = false
            _noteTags.value = emptyList()
            hasChanged = false
            initialLoadCompleted = true
            return
        }

        viewModelScope.launch {
            val note = getNoteByIdUseCase(id)
            if (note != null) {
                _noteId.value = note.id
                _title.value = note.title
                _blocks.value = if (note.content.isEmpty()) listOf(NoteBlock.ParagraphBlock("")) else note.content
                _selectedNotebookId.value = note.notebookId
                _coverColor.value = note.coverColor
                _iconSymbol.value = note.icon
                _isPinned.value = note.isPinned
                _isFavorite.value = note.isFavorite
                _noteTags.value = note.tags
            } else {
                _noteId.value = id
            }
            hasChanged = false
            initialLoadCompleted = true
        }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
        hasChanged = true
    }

    fun setCoverColor(color: String) {
        _coverColor.value = color
        hasChanged = true
        performInstantSave()
    }

    fun setIcon(icon: String) {
        _iconSymbol.value = icon
        hasChanged = true
        performInstantSave()
    }

    fun setNotebook(notebookId: String?) {
        _selectedNotebookId.value = notebookId
        hasChanged = true
        performInstantSave()
    }

    fun togglePinned() {
        _isPinned.value = !_isPinned.value
        hasChanged = true
        performInstantSave()
    }

    fun toggleFavorite() {
        _isFavorite.value = !_isFavorite.value
        hasChanged = true
        performInstantSave()
    }

    fun toggleNoteTag(tag: Tag) {
        val current = _noteTags.value.toMutableList()
        if (current.any { it.id == tag.id }) {
            current.removeAll { it.id == tag.id }
        } else {
            current.add(tag)
        }
        _noteTags.value = current
        hasChanged = true
        performInstantSave()
    }

    // Block Operations
    fun updateBlock(index: Int, block: NoteBlock) {
        if (index !in _blocks.value.indices) return
        val current = _blocks.value.toMutableList()
        current[index] = block
        _blocks.value = current
        hasChanged = true
    }

    fun insertBlock(index: Int, block: NoteBlock) {
        val current = _blocks.value.toMutableList()
        if (index >= current.size) {
            current.add(block)
        } else {
            current.add(index + 1, block)
        }
        _blocks.value = current
        hasChanged = true
    }

    fun deleteBlock(index: Int) {
        val current = _blocks.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(index)
        } else {
            current[0] = NoteBlock.ParagraphBlock("")
        }
        _blocks.value = current
        hasChanged = true
    }

    fun moveBlock(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in _blocks.value.indices || toIndex !in _blocks.value.indices) return
        val current = _blocks.value.toMutableList()
        val block = current.removeAt(fromIndex)
        current.add(toIndex, block)
        _blocks.value = current
        hasChanged = true
    }

    fun performInstantSave() {
        if (initialLoadCompleted) {
            performSave()
        }
    }

    private fun performSave() {
        val currentId = _noteId.value ?: return
        if (_title.value.isBlank() && _blocks.value.all { it is NoteBlock.ParagraphBlock && it.text.isBlank() }) {
            return // Skip saving fully blank note
        }

        _isSaving.value = true
        viewModelScope.launch {
            val noteToSave = Note(
                id = currentId,
                title = _title.value,
                content = _blocks.value,
                notebookId = _selectedNotebookId.value,
                coverColor = _coverColor.value,
                icon = _iconSymbol.value,
                isPinned = _isPinned.value,
                isFavorite = _isFavorite.value,
                isDeleted = false,
                tags = _noteTags.value
            )
            saveNoteUseCase(noteToSave)
            hasChanged = false
            _isSaving.value = false
        }
    }

    // Word Counts
    fun getWordCount(): Int {
        return _blocks.value.sumOf { block ->
            val text = when (block) {
                is NoteBlock.ParagraphBlock -> block.text
                is NoteBlock.HeadingBlock -> block.text
                is NoteBlock.BulletBlock -> block.text
                is NoteBlock.NumberedBlock -> block.text
                is NoteBlock.TodoBlock -> block.text
                is NoteBlock.QuoteBlock -> block.text
                is NoteBlock.CodeBlock -> block.code
                is NoteBlock.DividerBlock -> ""
            }
            if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
        }
    }

    fun getReadingTimeMinutes(): Int {
        val words = getWordCount()
        return maxOf(1, Math.round(words / 200.0).toInt())
    }
}

// --------------------------------------------------------
// 3. Notebooks List ViewModel
// --------------------------------------------------------
class NotebooksViewModel(
    private val notebookRepository: NotebookRepository
) : ViewModel() {

    val notebooks = notebookRepository.getAllNotebooks().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addNotebook(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val notebook = Notebook(
                id = UUID.randomUUID().toString(),
                name = name,
                colorHex = colorHex
            )
            notebookRepository.saveNotebook(notebook)
        }
    }

    fun updateNotebook(notebook: Notebook) {
        viewModelScope.launch {
            notebookRepository.saveNotebook(notebook)
        }
    }

    fun deleteNotebook(notebook: Notebook) {
        viewModelScope.launch {
            notebookRepository.deleteNotebook(notebook)
        }
    }
}

// --------------------------------------------------------
// 4. Tags List ViewModel
// --------------------------------------------------------
class TagsViewModel(
    private val tagRepository: TagRepository
) : ViewModel() {

    val tags = tagRepository.getAllTags().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addTag(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val tag = Tag(
                id = UUID.randomUUID().toString(),
                name = name,
                colorHex = colorHex
            )
            tagRepository.saveTag(tag)
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.deleteTag(tag)
        }
    }
}

// --------------------------------------------------------
// 5. Trash / Recycle Bin ViewModel
// --------------------------------------------------------
class TrashViewModel(
    private val getTrashedNotesUseCase: GetTrashedNotesUseCase,
    private val noteRepository: NoteRepository,
    private val deleteNotePermanentlyUseCase: DeleteNotePermanentlyUseCase
) : ViewModel() {

    val trashedNotes = getTrashedNotesUseCase().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun restoreNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.markAsTrash(noteId, false)
        }
    }

    fun deletePermanently(note: Note) {
        viewModelScope.launch {
            deleteNotePermanentlyUseCase(note)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            val items = trashedNotes.value
            items.forEach { note ->
                deleteNotePermanentlyUseCase(note)
            }
        }
    }
}

// --------------------------------------------------------
// 6. Settings Screen ViewModel
// --------------------------------------------------------
class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val context: android.content.Context
) : ViewModel() {

    val userName = settingsManager.userNameFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "Creator"
    )

    val themeMode = settingsManager.themeModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM
    )

    val fontSize = settingsManager.fontSizeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), FontSize.MEDIUM
    )

    val appLockEnabled = settingsManager.appLockEnabledFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val appLockPin = settingsManager.appLockPinFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )

    fun updateUserName(name: String) {
        viewModelScope.launch {
            settingsManager.setUserName(name)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsManager.setThemeMode(mode)
        }
    }

    fun updateFontSize(size: FontSize) {
        viewModelScope.launch {
            settingsManager.setFontSize(size)
        }
    }

    fun setAppLock(enabled: Boolean, pin: String) {
        viewModelScope.launch {
            settingsManager.setAppLockEnabled(enabled)
            settingsManager.setAppLockPin(pin)
        }
    }

    fun exportNoteAsTxt(note: Note) {
        viewModelScope.launch {
            try {
                val timestamp = System.currentTimeMillis()
                val filename = "NoteFlow_${note.title.replace("\\s+".toRegex(), "_")}_$timestamp.txt"
                
                val builder = java.lang.StringBuilder()
                builder.append("=== ${note.icon} ${note.title} ===\n\n")
                builder.append("Updated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(note.updatedAt))}\n")
                if (note.notebookName != null) {
                    builder.append("Notebook: ${note.notebookName}\n")
                }
                if (note.tags.isNotEmpty()) {
                    builder.append("Tags: ${note.tags.joinToString { it.name }}\n")
                }
                builder.append("\n----------------------------------------\n\n")

                note.content.forEach { block ->
                    when (block) {
                        is NoteBlock.ParagraphBlock -> builder.append(block.text).append("\n")
                        is NoteBlock.HeadingBlock -> {
                            val hashes = "#".repeat(block.level)
                            builder.append("$hashes ${block.text}").append("\n")
                        }
                        is NoteBlock.BulletBlock -> {
                            val indent = "  ".repeat(block.depth)
                            builder.append("$indent• ${block.text}").append("\n")
                        }
                        is NoteBlock.NumberedBlock -> {
                            builder.append("${block.index}. ${block.text}").append("\n")
                        }
                        is NoteBlock.TodoBlock -> {
                            val mark = if (block.isChecked) "[x]" else "[ ]"
                            builder.append("$mark ${block.text}").append("\n")
                        }
                        is NoteBlock.QuoteBlock -> builder.append("> ${block.text}").append("\n")
                        is NoteBlock.CodeBlock -> builder.append("```${block.language}\n${block.code}\n```").append("\n")
                        is NoteBlock.DividerBlock -> builder.append("--------------------").append("\n")
                    }
                    builder.append("\n")
                }

                val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                if (downloadDir != null) {
                    val file = java.io.File(downloadDir, filename)
                    file.writeText(builder.toString())
                    // Show a toast or notification
                    delay(10) // Small yield
                    android.widget.Toast.makeText(context, "Exported note to Downloads fold: $filename", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    android.widget.Toast.makeText(context, "Downloads directory unavailable", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Export failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
