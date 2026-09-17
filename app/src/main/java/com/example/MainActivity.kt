package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.components.NexBottomBar
import com.example.ui.screens.AiProcessingScreen
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CaptureScreen
import com.example.ui.screens.EditNoteScreen
import com.example.ui.screens.FollowUpsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoteDetailScreen
import com.example.ui.screens.NotesLibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.VoiceRecordingScreen
import com.example.ui.theme.NexDarkBackground
import com.example.ui.theme.NexNoteTheme
import com.example.viewmodel.NotelyViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: NotelyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val darkTheme by viewModel.darkThemeEnabled.collectAsState()

            NexNoteTheme(darkTheme = darkTheme) {
                NexNoteApp(viewModel = viewModel, activity = this)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        try {
            // Direct note opening from notification
            if (intent.hasExtra("open_note_id")) {
                val noteId = intent.getLongExtra("open_note_id", -1L)
                if (noteId > 0) {
                    viewModel.openNoteDetail(noteId)
                }
            }

            // Quick Settings Tile action
            if (intent.getBooleanExtra("open_capture_sheet", false)) {
                viewModel.navigateTo("CAPTURE")
            }

            // Widget Actions
            when (intent.getStringExtra("widget_action")) {
                "RECORD" -> {
                    requestRecordAudioPermission {
                        viewModel.startRecording()
                    }
                }
                "NOTE" -> viewModel.navigateTo("CAPTURE")
                "SCAN" -> viewModel.navigateTo("CAPTURE")
                "LINK" -> viewModel.navigateTo("CAPTURE")
                "TODAY" -> viewModel.navigateTo("CALENDAR")
                "HOME" -> viewModel.navigateTo("HOME")
            }

            // Handle Share Target
            if (Intent.ACTION_SEND == intent.action && intent.type != null) {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!sharedText.isNullOrBlank()) {
                    if (sharedText.startsWith("http://") || sharedText.startsWith("https://")) {
                        viewModel.processUrlCapture(sharedText)
                    } else {
                        viewModel.processTextInput(sharedText)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error processing intent", e)
        }
    }

    fun requestRecordAudioPermission(onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            // Permission requested via compose launcher in App Composable
            onGranted()
        }
    }
}

@Composable
fun NexNoteApp(viewModel: NotelyViewModel, activity: MainActivity) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeTab by viewModel.activeBottomTab.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    // Request audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        }
    }

    // Request notification permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun startVoiceWithPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startRecording()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // System Back Navigation Handling
    BackHandler(enabled = currentScreen != "HOME" && currentScreen != "SPLASH") {
        when (currentScreen) {
            "CAPTURE", "RECORD", "SEARCH", "SETTINGS", "STATS", "FOLLOW_UPS", "CALENDAR" -> {
                viewModel.navigateTo("HOME")
            }
            "NOTE_DETAIL" -> {
                viewModel.navigateTo("HOME")
            }
            "EDIT_NOTE" -> {
                viewModel.navigateTo("NOTE_DETAIL")
            }
            else -> {
                viewModel.navigateTo("HOME")
            }
        }
    }

    val showBottomBar = (currentScreen == "HOME" || currentScreen == "NOTES" || currentScreen == "ASSISTANT" || currentScreen == "CALENDAR") && !isProcessing

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showBottomBar) {
                NexBottomBar(
                    currentTab = activeTab,
                    onTabSelected = { tab ->
                        viewModel.activeBottomTab.value = tab
                        when (tab) {
                            "HOME" -> viewModel.navigateTo("HOME")
                            "NOTES" -> viewModel.navigateTo("NOTES")
                            "ASSISTANT" -> viewModel.navigateTo("ASSISTANT")
                            "CALENDAR" -> viewModel.navigateTo("CALENDAR")
                        }
                    },
                    onCaptureClick = { viewModel.navigateTo("CAPTURE") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isProcessing || currentScreen == "PROCESSING" -> {
                    AiProcessingScreen(viewModel = viewModel)
                }

                currentScreen == "SPLASH" -> {
                    SplashScreen(onContinue = { viewModel.navigateTo("HOME") })
                }

                currentScreen == "HOME" -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onOpenNote = { viewModel.openNoteDetail(it) },
                        onOpenSearch = { viewModel.navigateTo("SEARCH") },
                        onOpenFollowUps = { viewModel.navigateTo("FOLLOW_UPS") },
                        onOpenCalendar = { viewModel.navigateTo("CALENDAR") },
                        onOpenSettings = { viewModel.navigateTo("SETTINGS") },
                        onStartVoice = { startVoiceWithPermission() }
                    )
                }

                currentScreen == "NOTES" -> {
                    NotesLibraryScreen(
                        viewModel = viewModel,
                        onOpenNote = { viewModel.openNoteDetail(it) }
                    )
                }

                currentScreen == "ASSISTANT" -> {
                    AssistantScreen(
                        viewModel = viewModel,
                        onOpenNote = { viewModel.openNoteDetail(it) },
                        onStartVoice = { startVoiceWithPermission() }
                    )
                }

                currentScreen == "CAPTURE" -> {
                    CaptureScreen(
                        viewModel = viewModel,
                        onClose = { viewModel.navigateTo("HOME") },
                        onStartVoice = { startVoiceWithPermission() }
                    )
                }

                currentScreen == "RECORD" -> {
                    VoiceRecordingScreen(
                        viewModel = viewModel,
                        onCancel = { viewModel.navigateTo("HOME") }
                    )
                }

                currentScreen == "NOTE_DETAIL" -> {
                    NoteDetailScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("HOME") },
                        onEditNote = { viewModel.navigateTo("EDIT_NOTE") },
                        onOpenRelatedNote = { viewModel.openNoteDetail(it) },
                        onNavigateToCalendar = { viewModel.navigateTo("CALENDAR") },
                        onNavigateToFollowUps = { viewModel.navigateTo("FOLLOW_UPS") }
                    )
                }

                currentScreen == "FOLLOW_UPS" -> {
                    FollowUpsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("HOME") },
                        onOpenNote = { viewModel.openNoteDetail(it) }
                    )
                }

                currentScreen == "CALENDAR" -> {
                    CalendarScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("HOME") },
                        onOpenNote = { viewModel.openNoteDetail(it) }
                    )
                }

                currentScreen == "SEARCH" -> {
                    SearchScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("HOME") },
                        onOpenNote = { viewModel.openNoteDetail(it) }
                    )
                }

                currentScreen == "EDIT_NOTE" -> {
                    EditNoteScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("NOTE_DETAIL") }
                    )
                }

                currentScreen == "SETTINGS" -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("HOME") }
                    )
                }

                currentScreen == "STATS" -> {
                    StatsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("HOME") }
                    )
                }
            }
        }
    }
}
