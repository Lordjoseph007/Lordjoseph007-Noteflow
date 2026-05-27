package com.example.presentation.tags

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
import com.example.domain.model.Tag
import com.example.presentation.TagsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    viewModel: TagsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tags by viewModel.tags.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var tagNameInput by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#FFD700") }

    val presetColors = listOf(
        "#FFD700", "#E76F51", "#2A9D8F", "#457B9D",
        "#8338EC", "#F4A261", "#E63946", "#EC5E94",
        "#1D3557", "#264653", "#70E000", "#E24A35",
        "#A8DADC", "#3A86C8", "#FF007F", "#00F5D4"
    )

    Scaffold(
        modifier = modifier.testTag("tags_screen_layout"),
        topBar = {
            TopAppBar(
                title = { Text("Tags System", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tags_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        tagNameInput = ""
                        selectedColorHex = "#FFD700"
                        showCreateDialog = true
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Create Tag")
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
            if (tags.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Tags Created Yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap the '+' button above to generate custom stickers, labels, and tags.",
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
                    items(tags) { tag ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tag_item_${tag.id}"),
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
                                        .background(Color(android.graphics.Color.parseColor(tag.colorHex)))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tag.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Color Hex: ${tag.colorHex}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteTag(tag) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete tag",
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
                title = { Text("Create Custom Tag") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = tagNameInput,
                            onValueChange = { tagNameInput = it },
                            placeholder = { Text("Important, Work, Personal, Later...") },
                            label = { Text("Tag Name") },
                            modifier = Modifier.fillMaxWidth().testTag("tag_name_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Select Tag Tag Color Accent", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
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
                            if (tagNameInput.isNotBlank()) {
                                viewModel.addTag(tagNameInput, selectedColorHex)
                                showCreateDialog = false
                            }
                        },
                        modifier = Modifier.testTag("tag_confirm_btn")
                    ) {
                        Text("Save")
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
