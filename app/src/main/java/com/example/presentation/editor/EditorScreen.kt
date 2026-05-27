package com.example.presentation.editor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.NoteBlock
import java.util.UUID
import com.example.domain.model.Tag
import com.example.domain.model.TextFormatting
import com.example.presentation.EditorViewModel
import com.example.ui.components.CoverPicker
import com.example.ui.components.EmojiPicker
import com.example.ui.components.getCoverBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    noteId: String?,
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    val title by viewModel.title
    val blocks by viewModel.blocks
    val selectedNotebookId by viewModel.selectedNotebookId
    val coverColor by viewModel.coverColor
    val iconSymbol by viewModel.iconSymbol
    val isPinned by viewModel.isPinned
    val isFavorite by viewModel.isFavorite
    val isSaving by viewModel.isSaving
    val noteTags by viewModel.noteTags

    val notebooks by viewModel.notebooks.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    var showNotebookMenu by remember { mutableStateOf(false) }
    var showTagMenu by remember { mutableStateOf(false) }
    var showCoverChooser by remember { mutableStateOf(false) }
    var showEmojiChooser by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("editor_screen_layout"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSaving) {
                            Text(
                                "Saving...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.animateContentSize()
                            )
                        } else {
                            Text(
                                "Saved",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.performInstantSave()
                            onBack()
                        },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    // Star Favorite
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleFavorite()
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite Toggle",
                            tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Pin Note
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.togglePinned()
                    }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Toggle",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Assign Notebook
                    IconButton(onClick = { showNotebookMenu = true }) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Assign notebook")
                    }

                    // Assign Tag
                    IconButton(onClick = { showTagMenu = true }) {
                        Icon(imageVector = Icons.Default.LocalOffer, contentDescription = "Tags Management")
                    }
                }
            )
        },
        bottomBar = {
            // Stats footer (satisfies: word count and reading time shown at bottom)
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Words: ${viewModel.getWordCount()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                    Text(
                        text = "Reading time: ${viewModel.getReadingTimeMinutes()} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Content Box
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Cover Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(getCoverBrush(coverColor))
                        .clickable { showCoverChooser = !showCoverChooser },
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Surface(
                        color = Color.Black.copy(0.4f),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(topStart = 8.dp),
                        modifier = Modifier.padding(1.dp)
                    ) {
                        Text(
                            text = "Tap to Change Cover",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                // Emoji Icon bubble
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .offset(y = (-30).dp)
                        .size(68.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .clickable { showEmojiChooser = !showEmojiChooser },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconSymbol, fontSize = 36.sp)
                }

                Column(
                    modifier = Modifier
                        .offset(y = (-20).dp)
                        .padding(horizontal = 16.dp)
                ) {
                    // Quick choose editors
                    if (showCoverChooser) {
                        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                CoverPicker(selectedCover = coverColor, onCoverSelected = {
                                    viewModel.setCoverColor(it)
                                })
                            }
                        }
                    }

                    if (showEmojiChooser) {
                        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                EmojiPicker(selectedEmoji = iconSymbol, onEmojiSelected = {
                                    viewModel.setIcon(it)
                                    showEmojiChooser = false
                                })
                            }
                        }
                    }

                    // Display Current Notebook and Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val activeNotebook = notebooks.find { it.id == selectedNotebookId }
                        if (activeNotebook != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(0.15f),
                                contentColor = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(activeNotebook.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Tags Row
                        noteTags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(android.graphics.Color.parseColor(tag.colorHex)).copy(0.15f),
                                contentColor = Color(android.graphics.Color.parseColor(tag.colorHex)),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    tag.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Main Title
                    TextField(
                        value = title,
                        onValueChange = { viewModel.updateTitle(it) },
                        placeholder = { Text("Note Title", style = MaterialTheme.typography.displayMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(0.3f))) },
                        textStyle = MaterialTheme.typography.displayMedium,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_title_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Block Editor Area
                    blocks.forEachIndexed { idx, block ->
                        BlockItemRow(
                            index = idx,
                            block = block,
                            onUpdate = { updated -> viewModel.updateBlock(idx, updated) },
                            onDelete = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteBlock(idx)
                            },
                            onMoveUp = if (idx > 0) { { viewModel.moveBlock(idx, idx - 1) } } else null,
                            onMoveDown = if (idx < blocks.size - 1) { { viewModel.moveBlock(idx, idx + 1) } } else null,
                            onEnterPressed = {
                                viewModel.insertBlock(idx, NoteBlock.ParagraphBlock(""))
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Insert buttons
                    Button(
                        onClick = {
                            viewModel.insertBlock(blocks.size - 1, NoteBlock.ParagraphBlock(""))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("editor_add_block_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Block")
                    }
                }
            }

            // Notebook selection dialogue
            if (showNotebookMenu) {
                AlertDialog(
                    onDismissRequest = { showNotebookMenu = false },
                    title = { Text("Select Folder") },
                    text = {
                        Column {
                            // Option for no notebook
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setNotebook(null)
                                        showNotebookMenu = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isSelected = selectedNotebookId == null
                                RadioButton(selected = isSelected, onClick = {
                                    viewModel.setNotebook(null)
                                    showNotebookMenu = false
                                })
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.FolderOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("No notebook folder", style = MaterialTheme.typography.bodyLarge)
                            }

                            notebooks.forEach { folder ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setNotebook(folder.id)
                                            showNotebookMenu = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isSelected = selectedNotebookId == folder.id
                                    RadioButton(selected = isSelected, onClick = {
                                        viewModel.setNotebook(folder.id)
                                        showNotebookMenu = false
                                    })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = Color(android.graphics.Color.parseColor(folder.colorHex)))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(folder.name, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showNotebookMenu = false }) { Text("Dismiss") }
                    }
                )
            }

            // Tag selection dialogue
            if (showTagMenu) {
                AlertDialog(
                    onDismissRequest = { showTagMenu = false },
                    title = { Text("Manage Note Tags") },
                    text = {
                        Column {
                            if (allTags.isEmpty()) {
                                Text("No tags created yet! Create tags from the tags menu in the home sidebar.", modifier = Modifier.padding(8.dp))
                            }
                            allTags.forEach { tag ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.toggleNoteTag(tag)
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isSelected = noteTags.any { it.id == tag.id }
                                    Checkbox(checked = isSelected, onCheckedChange = {
                                        viewModel.toggleNoteTag(tag)
                                    })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(tag.colorHex)))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tag.name, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showTagMenu = false }) { Text("Completed") }
                    }
                )
            }
        }
    }
}

@Composable
fun BlockItemRow(
    index: Int,
    block: NoteBlock,
    onUpdate: (NoteBlock) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onEnterPressed: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("editor_block_${index}"),
        verticalAlignment = Alignment.Top
    ) {
        // Drag/Dots Menu Button
        Box {
            IconButton(
                onClick = { expandedMenu = true },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("block_menu_${index}")
            ) {
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Block settings",
                    tint = MaterialTheme.colorScheme.onSurface.copy(0.3f)
                )
            }

            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false }
            ) {
                // Change block types options
                Text(
                    "Change Type",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                DropdownMenuItem(
                    text = { Text("Paragraph") },
                    onClick = {
                        onUpdate(NoteBlock.ParagraphBlock(block.getText()))
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.ShortText, null) }
                )
                DropdownMenuItem(
                    text = { Text("Heading 1") },
                    onClick = {
                        onUpdate(NoteBlock.HeadingBlock(block.getText(), 1))
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.FormatSize, null) }
                )
                DropdownMenuItem(
                    text = { Text("Heading 2") },
                    onClick = {
                        onUpdate(NoteBlock.HeadingBlock(block.getText(), 2))
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.FormatSize, null) }
                )
                DropdownMenuItem(
                    text = { Text("Heading 3") },
                    onClick = {
                        onUpdate(NoteBlock.HeadingBlock(block.getText(), 3))
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.FormatSize, null) }
                )
                DropdownMenuItem(
                    text = { Text("Bulleted List") },
                    onClick = {
                        onUpdate(NoteBlock.BulletBlock(block.getText(), 0))
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.FormatListBulleted, null) }
                )
                DropdownMenuItem(
                    text = { Text("Numbered List") },
                    onClick = {
                        onUpdate(NoteBlock.NumberedBlock(block.getText(), index + 1))
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.FormatListNumbered, null) }
                )
                DropdownMenuItem(
                    text = { Text("Checkbox Checklist") },
                    onClick = {
                        onUpdate(NoteBlock.TodoBlock(block.getText(), false))
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.CheckBox, null) }
                )
                DropdownMenuItem(
                    text = { Text("Quote Block") },
                    onClick = {
                        onUpdate(NoteBlock.QuoteBlock(block.getText()))
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.FormatQuote, null) }
                )
                DropdownMenuItem(
                    text = { Text("Code block") },
                    onClick = {
                        onUpdate(NoteBlock.CodeBlock(block.getText(), "kotlin"))
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Code, null) }
                )
                DropdownMenuItem(
                    text = { Text("Divider") },
                    onClick = {
                        onUpdate(NoteBlock.DividerBlock(UUID.randomUUID().toString()))
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.HorizontalRule, null) }
                )

                HorizontalDivider()

                // Move and Delete Options
                if (onMoveUp != null) {
                    DropdownMenuItem(
                        text = { Text("Move Up") },
                        onClick = {
                            onMoveUp()
                            expandedMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.ArrowUpward, null) }
                    )
                }
                if (onMoveDown != null) {
                    DropdownMenuItem(
                        text = { Text("Move Down") },
                        onClick = {
                            onMoveDown()
                            expandedMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.ArrowDownward, null) }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Delete Block", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        onDelete()
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Inner TextField matching block type
        Box(modifier = Modifier.weight(1f).padding(top = 2.dp)) {
            when (block) {
                is NoteBlock.ParagraphBlock -> {
                    TextField(
                        value = block.text,
                        onValueChange = { onUpdate(block.copy(text = it)) },
                        placeholder = { Text("Tap to write paragraph...", style = TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(0.3f))) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = getTextStyle(block.formatting),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is NoteBlock.HeadingBlock -> {
                    val scale = when (block.level) {
                        1 -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                        2 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                        else -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    }
                    TextField(
                        value = block.text,
                        onValueChange = { onUpdate(block.copy(text = it)) },
                        placeholder = { Text("Heading ${block.level}", style = scale.copy(color = MaterialTheme.colorScheme.onSurface.copy(0.3f))) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = scale,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is NoteBlock.BulletBlock -> {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("•", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        TextField(
                            value = block.text,
                            onValueChange = { onUpdate(block.copy(text = it)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is NoteBlock.NumberedBlock -> {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("${block.index}.", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        TextField(
                            value = block.text,
                            onValueChange = { onUpdate(block.copy(text = it)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is NoteBlock.TodoBlock -> {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = block.isChecked,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onUpdate(block.copy(isChecked = it))
                            },
                            modifier = Modifier.size(40.dp)
                        )
                        TextField(
                            value = block.text,
                            onValueChange = { onUpdate(block.copy(text = it)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = if (block.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (block.isChecked) MaterialTheme.colorScheme.onSurface.copy(0.4f) else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is NoteBlock.QuoteBlock -> {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TextField(
                            value = block.text,
                            onValueChange = { onUpdate(block.copy(text = it)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is NoteBlock.CodeBlock -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(0.05f))
                            .padding(8.dp)
                    ) {
                        Text(
                            block.language.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                        TextField(
                            value = block.code,
                            onValueChange = { onUpdate(block.copy(code = it)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is NoteBlock.DividerBlock -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(0.15f))
                    )
                }
            }
        }
    }
}

// Extension to retrieve raw text string from block
fun NoteBlock.getText(): String {
    return when (this) {
        is NoteBlock.ParagraphBlock -> text
        is NoteBlock.HeadingBlock -> text
        is NoteBlock.BulletBlock -> text
        is NoteBlock.NumberedBlock -> text
        is NoteBlock.TodoBlock -> text
        is NoteBlock.QuoteBlock -> text
        is NoteBlock.CodeBlock -> code
        is NoteBlock.DividerBlock -> ""
    }
}

@Composable
fun getTextStyle(formatting: TextFormatting): androidx.compose.ui.text.TextStyle {
    return TextStyle(
        fontWeight = if (formatting.isBold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (formatting.isItalic) FontStyle.Italic else FontStyle.Normal,
        textDecoration = when {
            formatting.isUnderline && formatting.isStrikethrough -> TextDecoration.combine(
                listOf(TextDecoration.Underline, TextDecoration.LineThrough)
            )
            formatting.isUnderline -> TextDecoration.Underline
            formatting.isStrikethrough -> TextDecoration.LineThrough
            else -> TextDecoration.None
        }
    )
}
