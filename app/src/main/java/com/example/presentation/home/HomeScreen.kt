package com.example.presentation.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Note
import com.example.presentation.HomeViewModel
import com.example.ui.components.NoteCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToEditor: (String?) -> Unit,
    onNavigateToNotebooks: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val greeting by viewModel.greeting.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedNotebookId by viewModel.selectedNotebookId.collectAsState()
    val selectedTagId by viewModel.selectedTagId.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()

    val notebooks by viewModel.notebooks.collectAsState()
    val tags by viewModel.tags.collectAsState()

    // Seed sample notes if app is totally blank
    LaunchedEffect(Unit) {
        viewModel.createSampleNotes()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "NoteFlow Flow",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Drawer Links
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home Launcher") },
                    selected = selectedNotebookId == null && selectedTagId == null && !showOnlyFavorites,
                    onClick = {
                        viewModel.clearFilters()
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).testTag("drawer_home")
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                    label = { Text("Notebook Folders") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToNotebooks()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).testTag("drawer_notebooks")
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
                    label = { Text("Tags System") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToTags()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).testTag("drawer_tags")
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Favorites Note list") },
                    selected = showOnlyFavorites,
                    onClick = {
                        viewModel.toggleFavoritesFilter()
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).testTag("drawer_favorites")
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    label = { Text("Trash Bin") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToTrash()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).testTag("drawer_trash")
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("AppSettings") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToSettings()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).testTag("drawer_settings")
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Folders list in drawer
                Text(
                    "FOLDERS",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                )

                notebooks.forEach { folder ->
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = null, tint = Color(android.graphics.Color.parseColor(folder.colorHex))) },
                        label = { Text(folder.name) },
                        selected = selectedNotebookId == folder.id,
                        onClick = {
                            viewModel.selectNotebook(folder.id)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "NoteFlow",
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu Drawer")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Toggle Grid/List View"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onNavigateToEditor(null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("floating_add_note_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create note")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Greeting text
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search your notes...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank() || selectedNotebookId != null || selectedTagId != null || showOnlyFavorites) {
                            IconButton(onClick = { viewModel.clearFilters() }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Filters")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("search_notes_input")
                )

                // Selected filter indicator chips
                if (selectedNotebookId != null || selectedTagId != null || showOnlyFavorites) {
                    Row(
                        modifier = Modifier.padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Filter: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        val label = when {
                            showOnlyFavorites -> "Favorites ⭐"
                            selectedNotebookId != null -> "Folder: ${notebooks.find { it.id == selectedNotebookId }?.name ?: ""}"
                            selectedTagId != null -> "Tag: ${tags.find { it.id == selectedTagId }?.name ?: ""}"
                            else -> ""
                        }
                        SuggestionChip(
                            onClick = { viewModel.clearFilters() },
                            label = { Text(label) },
                            icon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }

                if (notes.isEmpty()) {
                    // Beautiful custom illustrated empty states (satisfies design empty state requirement)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NoteAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                modifier = Modifier.size(92.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Thoughts waiting to Flow...",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No notes match active query. Tap the '+' button below to start creating beautiful block notes styled like Notion.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Pinned notes section vs all notes section
                    val pinnedList = notes.filter { it.isPinned }
                    val unpinnedList = notes.filter { !it.isPinned }

                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (pinnedList.isNotEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                    Text(
                                        "PINNED",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(pinnedList) { note ->
                                    NoteCard(
                                        note = note,
                                        onClick = { onNavigateToEditor(note.id) },
                                        onLongClick = { viewModel.deleteNoteToTrash(note.id) }
                                    )
                                }
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                    Text(
                                        "ALL NOTES",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }
                            items(unpinnedList) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { onNavigateToEditor(note.id) },
                                    onLongClick = { viewModel.deleteNoteToTrash(note.id) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (pinnedList.isNotEmpty()) {
                                item {
                                    Text(
                                        "PINNED",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(pinnedList) { note ->
                                    NoteCard(
                                        note = note,
                                        onClick = { onNavigateToEditor(note.id) },
                                        onLongClick = { viewModel.deleteNoteToTrash(note.id) }
                                    )
                                }
                                item {
                                    Text(
                                        "ALL NOTES",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }
                            items(unpinnedList) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { onNavigateToEditor(note.id) },
                                    onLongClick = { viewModel.deleteNoteToTrash(note.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
