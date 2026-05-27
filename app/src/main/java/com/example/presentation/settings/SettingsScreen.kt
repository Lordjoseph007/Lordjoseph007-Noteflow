package com.example.presentation.settings

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ThemeMode
import com.example.data.local.FontSize
import com.example.presentation.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    val appLockPin by viewModel.appLockPin.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var tempPinInput by remember { mutableStateOf("") }
    var isPinSetRequested by remember { mutableStateOf(false) }

    var editingUsername by remember { mutableStateOf(false) }
    var usernameFieldInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    LaunchedEffect(userName) {
        usernameFieldInput = userName
    }

    Scaffold(
        modifier = modifier.testTag("settings_screen_layout"),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Part 1: Creator Bio
            Text(
                "Creator Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (editingUsername) {
                        OutlinedTextField(
                            value = usernameFieldInput,
                            onValueChange = { usernameFieldInput = it },
                            label = { Text("Your Display Name") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (usernameFieldInput.isNotBlank()) {
                                        viewModel.updateUserName(usernameFieldInput)
                                        editingUsername = false
                                    }
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("username_input_field")
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Display Greeting Name", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                                Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { editingUsername = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit username")
                            }
                        }
                    }
                }
            }

            // Part 2: Visual Themes
            Text(
                "Screen Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Theme Setting Selection", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ThemeMode.entries.forEach { mode ->
                            val isSelected = themeMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateThemeMode(mode) },
                                label = { Text(mode.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Text Display Font Size Scale", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        FontSize.entries.forEach { size ->
                            val isSelected = fontSize == size
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateFontSize(size) },
                                label = { Text(size.name) }
                            )
                        }
                    }
                }
            }

            // Part 3: Lock/Security Setup
            Text(
                "App Security Pin Lock",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Setup Security PIN Lock", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (appLockEnabled) "App requires 4-digit PIN setup" else "Security PIN lock disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = appLockEnabled,
                            onCheckedChange = { check ->
                                if (check) {
                                    tempPinInput = ""
                                    isPinSetRequested = true
                                    showPinDialog = true
                                } else {
                                    viewModel.setAppLock(false, "")
                                }
                            },
                            modifier = Modifier.testTag("app_lock_switch")
                        )
                    }
                }
            }

            // Part 4: Version/Info
            Text(
                "Info & About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("NoteFlow Note Taking", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("Version 1.0.0 (Release Build v1)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "An advanced, local-first block editor inspired by Notion features, built entirely inside Jetpack Compose and local Room Database.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
            }
        }

        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text("Configure Security PIN") },
                text = {
                    Column {
                        Text("Enter a 4-digit numerical code to lock NoteFlow upon startup:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = tempPinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                    tempPinInput = it
                                }
                            },
                            placeholder = { Text("xxxx") },
                            label = { Text("4-digit PIN") },
                            modifier = Modifier.fillMaxWidth().testTag("temp_pin_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempPinInput.length == 4) {
                                viewModel.setAppLock(true, tempPinInput)
                                showPinDialog = false
                            }
                        },
                        modifier = Modifier.testTag("pin_confirm_id")
                    ) {
                        Text("Save PIN")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
