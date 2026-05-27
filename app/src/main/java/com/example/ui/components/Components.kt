package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Note
import com.example.domain.model.NoteBlock
import com.example.domain.model.Tag

// Color Gradients map for Cover selection
val CoverGradients = mapOf(
    "gradient_indigo" to listOf(Color(0xFF3D2C8D), Color(0xFF1E144F)),
    "gradient_sunset" to listOf(Color(0xFFF4A261), Color(0xFFE76F51)),
    "gradient_ocean" to listOf(Color(0xFF1A759F), Color(0xFF1E6091)),
    "gradient_emerald" to listOf(Color(0xFF2D6A4F), Color(0xFF52B788)),
    "gradient_crimson" to listOf(Color(0xFF9B2226), Color(0xFFAE2012)),
    "gradient_lemon" to listOf(Color(0xFFE9C46A), Color(0xFFD81E5B)),
    "gradient_charcoal" to listOf(Color(0xFF2C3E50), Color(0xFF000000)),
    "gradient_aurora" to listOf(Color(0xFF00B4D8), Color(0xFF90E0EF))
)

fun getCoverBrush(coverKey: String): Brush {
    val colors = CoverGradients[coverKey] ?: listOf(Color(0xFF3D2C8D), Color(0xFF1E144F))
    return Brush.linearGradient(colors)
}

@Composable
fun TagChip(
    tag: Tag,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .testTag("tag_chip_${tag.name}")
            .clip(RoundedCornerShape(6.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = Color(android.graphics.Color.parseColor(tag.colorHex)).copy(alpha = 0.15f),
        contentColor = Color(android.graphics.Color.parseColor(tag.colorHex))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalOffer,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = Color(android.graphics.Color.parseColor(tag.colorHex))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = tag.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteClick: (() -> Unit)? = null,
    onPinClick: (() -> Unit)? = null
) {
    // Generate text preview from block content
    val previewText = note.content.take(3).mapNotNull { block ->
        when (block) {
            is NoteBlock.ParagraphBlock -> block.text
            is NoteBlock.HeadingBlock -> block.text
            is NoteBlock.BulletBlock -> block.text
            is NoteBlock.NumberedBlock -> block.text
            is NoteBlock.TodoBlock -> block.text
            is NoteBlock.QuoteBlock -> block.text
            is NoteBlock.CodeBlock -> block.code
            is NoteBlock.DividerBlock -> null
        }
    }.joinToString(" ").take(100)

    val updatedDateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        .format(java.util.Date(note.updatedAt))

    @OptIn(ExperimentalFoundationApi::class)
    Card(
        modifier = modifier
            .padding(vertical = 6.dp)
            .testTag("note_item_card_${note.id}")
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Gradient Header Band
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(getCoverBrush(note.coverColor))
            ) {
                // Pin Badge inside Cover band
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned note",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(16.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                            .padding(2.dp)
                    )
                }
                
                if (note.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorite note",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .size(16.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                            .padding(2.dp)
                    )
                }
            }

            // Body Area
            Column(modifier = Modifier.padding(12.dp)) {
                // Header Row (Emoji + Title)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = note.icon,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = note.title.ifBlank { "Untitled Note" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Notebook Folder Association
                if (!note.notebookName.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Folder",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = note.notebookName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Text preview
                if (previewText.isNotBlank()) {
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer Row: Tags + Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Scrolling tags inside card
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(note.tags) { tag ->
                            TagChip(tag = tag)
                        }
                    }

                    Text(
                        text = updatedDateStr,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CoverPicker(
    selectedCover: String,
    onCoverSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Select Cover Gradient",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CoverGradients.keys.toList()) { key ->
                val gradientColors = CoverGradients[key] ?: listOf(Color.Gray)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.linearGradient(gradientColors))
                        .clickable { onCoverSelected(key) }
                        .then(
                            if (selectedCover == key) Modifier.background(
                                Color.White.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedCover == key) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier
                                .background(Color.Black.copy(0.4f), CircleShape)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = listOf(
        "📝", "💡", "🎯", "📌", "💼", "📅", "🚀", "🎨", "💻", "📂",
        "🔐", "🔑", "🍿", "🏠", "🍕", "🍔", "☕", "🍺", "🍷", "🍾",
        "🐈", "🐕", "🐘", "🦉", "🦁", "🦖", "🌲", "🌟", "🔥", "🌈",
        "🥇", "🏆", "🚗", "⛵", "✈️", "🗺️", "🎸", "🎧", "🎬", "📚"
    )

    Column(modifier = modifier) {
        Text(
            text = "Select Icon Emoji",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedEmoji == emoji) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }
        }
    }
}
