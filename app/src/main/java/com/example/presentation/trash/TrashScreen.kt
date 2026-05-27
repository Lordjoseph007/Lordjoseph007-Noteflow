package com.example.presentation.trash

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.Note
import com.example.presentation.TrashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trashedNotes by viewModel.trashedNotes.collectAsState()
    var showEmptyTrashConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("trash_screen_layout"),
        topBar = {
            TopAppBar(
                title = { Text("Trash Bin", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("trash_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    if (trashedNotes.isNotEmpty()) {
                        IconButton(onClick = { showEmptyTrashConfirm = true }) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Empty Trash Bin")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (trashedNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your Trash is Empty",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Notes sent to trash will appear here. Recover them anytime.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(trashedNotes) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trashed_item_${note.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(note.icon, style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = note.title.ifBlank { "Untitled Note" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Deleted folder note",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }

                                IconButton(onClick = { viewModel.restoreNote(note.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = "Restore discarded note",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(onClick = { viewModel.deletePermanently(note) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = "Delete permanently",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showEmptyTrashConfirm) {
            AlertDialog(
                onDismissRequest = { showEmptyTrashConfirm = false },
                title = { Text("Empty Trash?") },
                text = { Text("Are you absolutely sure you want to empty the trash? All items will be permanently deleted and cannot be recovered.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.emptyTrash()
                            showEmptyTrashConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("trash_confirm_empty_btn")
                    ) {
                        Text("Delete All", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmptyTrashConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
