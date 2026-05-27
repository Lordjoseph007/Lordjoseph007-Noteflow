package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.presentation.*
import com.example.presentation.home.HomeScreen
import com.example.presentation.editor.EditorScreen
import com.example.presentation.notebooks.NotebooksScreen
import com.example.presentation.tags.TagsScreen
import com.example.presentation.trash.TrashScreen
import com.example.presentation.settings.SettingsScreen
import com.example.ui.theme.NoteFlowTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (applicationContext as NoteFlowApplication).container
        val settingsManager = appContainer.settingsManager

        // Read initial Lock preferences blocking to avoid flicker on start
        var isLockedInitial = false
        var correctPinInitial = ""
        runBlocking {
            isLockedInitial = settingsManager.appLockEnabledFlow.first()
            correctPinInitial = settingsManager.appLockPinFlow.first()
        }

        setContent {
            NoteFlowTheme {
                var isAppLocked by remember { mutableStateOf(isLockedInitial) }
                val correctPin by remember { mutableStateOf(correctPinInitial) }

                if (isAppLocked && correctPin.isNotBlank()) {
                    PinLockScreen(
                        correctPin = correctPin,
                        onUnlocked = { isAppLocked = false },
                        onInvokeBiometrics = {
                            showBiometricPrompt {
                                isAppLocked = false
                            }
                        }
                    )
                } else {
                    val navController = rememberNavController()
                    val homeViewModel: HomeViewModel = viewModel(factory = ViewModelFactory(this, appContainer))
                    val editorViewModel: EditorViewModel = viewModel(factory = ViewModelFactory(this, appContainer))
                    val notebooksViewModel: NotebooksViewModel = viewModel(factory = ViewModelFactory(this, appContainer))
                    val tagsViewModel: TagsViewModel = viewModel(factory = ViewModelFactory(this, appContainer))
                    val trashViewModel: TrashViewModel = viewModel(factory = ViewModelFactory(this, appContainer))
                    val settingsViewModel: SettingsViewModel = viewModel(factory = ViewModelFactory(this, appContainer))

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToEditor = { id -> navController.navigate("editor?noteId=$id") },
                                onNavigateToNotebooks = { navController.navigate("notebooks") },
                                onNavigateToTags = { navController.navigate("tags") },
                                onNavigateToTrash = { navController.navigate("trash") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }

                        composable(
                            route = "editor?noteId={noteId}",
                            arguments = listOf(navArgument("noteId") { type = NavType.StringType; nullable = true; defaultValue = null })
                        ) { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getString("noteId")
                            EditorScreen(
                                noteId = noteId,
                                viewModel = editorViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("notebooks") {
                            NotebooksScreen(
                                viewModel = notebooksViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("tags") {
                            TagsScreen(
                                viewModel = tagsViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("trash") {
                            TrashScreen(
                                viewModel = trashViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Auth Error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Biometric login successful!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("NoteFlow Security Quick Unlock")
            .setSubtitle("Authenticate using your fingerprint or biometric profile")
            .setNegativeButtonText("Use PIN code as fallback")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Biometrics unavailable on this hardware", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun PinLockScreen(
    correctPin: String,
    onUnlocked: () -> Unit,
    onInvokeBiometrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var accumulatedPin by remember { mutableStateOf("") }
    var displaysError by remember { mutableStateOf(false) }

    LaunchedEffect(accumulatedPin) {
        if (accumulatedPin.length == 4) {
            if (accumulatedPin == correctPin) {
                onUnlocked()
            } else {
                displaysError = true
                accumulatedPin = ""
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(0.1f), CircleShape)
                    .padding(18.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "NoteFlow Secured",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Please authenticate using your 4-digit PIN code",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pin indicators Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..3) {
                    val isActive = i < accumulatedPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (displaysError) MaterialTheme.colorScheme.error
                                else if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            if (displaysError) {
                Text(
                    "Incorrect PIN, please try again",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Premium Visual Numpad Row
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val numpadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("Del", "0", "Bio")
                )

                numpadRows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        row.forEach { char ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (char == "Bio") MaterialTheme.colorScheme.primary.copy(0.15f)
                                        else MaterialTheme.colorScheme.onSurface.copy(0.06f)
                                    )
                                    .clickable {
                                        displaysError = false
                                        when (char) {
                                            "Del" -> {
                                                if (accumulatedPin.isNotEmpty()) {
                                                    accumulatedPin = accumulatedPin.dropLast(1)
                                                }
                                            }
                                            "Bio" -> {
                                                onInvokeBiometrics()
                                            }
                                            else -> {
                                                if (accumulatedPin.length < 4) {
                                                    accumulatedPin += char
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (char == "Bio") {
                                    Icon(Icons.Default.Fingerprint, contentDescription = "Use Biometrics Fingerprint", tint = MaterialTheme.colorScheme.primary)
                                } else {
                                    Text(
                                        text = char,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (char == "Del") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
