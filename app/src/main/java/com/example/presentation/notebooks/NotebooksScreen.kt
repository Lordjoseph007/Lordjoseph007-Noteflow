package com.example.presentation.notebooks

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Notebook
import com.example.presentation.NotebooksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebooksScreen(
    viewModel: NotebooksViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notebooks by viewModel.notebooks.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var notebookNameInput by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#3D2C8D") }

    val presetColors = listOf(
        "#3D2C8D", "#264653", "#2A9D8F", "#E9C46A",
        "#F4A261", "#E76F51", "#212529", "#E63946",
        "#457B9D", "#1D3557", "#A8DADC", "#8338EC",
        "#3A86C8", "#FF007F", "#00F5D4", "#70E000"
    )

    Scaffold(
        modifier = modifier.testTag("notebooks_screen_layout"),
        topBar = {
            TopAppBar(
                title = { Text("Notebook Folders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notebooks_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        notebookNameInput = ""
                        selectedColorHex = "#3D2C8D"
                        showCreateDialog = true
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Create Notebook Folder")
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
            if (notebooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(0.3f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Folder Directories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap the '+' button at the top to organize notes inside tailored notebooks.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(notebooks) { notebook ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notebook_item_${notebook.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(notebook.colorHex)))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notebook.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Folder ID: ${notebook.id.take(8)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteNotebook(notebook) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete notebook folder",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create Notebook Folder") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = notebookNameInput,
                            onValueChange = { notebookNameInput = it },
                            placeholder = { Text("School notes, Office, Recipes...") },
                            label = { Text("Folder Name") },
                            modifier = Modifier.fillMaxWidth().testTag("notebook_name_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Select Folder Color Accent", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.height(110.dp)
                        ) {
                            items(presetColors) { color ->
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(30.dp)
                                        .background(Color(android.graphics.Color.parseColor(color)), CircleShape)
                                        .clip(CircleShape)
                                        .clickable { selectedColorHex = color }
                                ) {
                                    if (selectedColorHex == color) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Picked",
                                            tint = Color.White,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (notebookNameInput.isNotBlank()) {
                                viewModel.addNotebook(notebookNameInput, selectedColorHex)
                                showCreateDialog = false
                            }
                        },
                        modifier = Modifier.testTag("notebook_confirm_btn")
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
