package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.ExportFormat
import com.example.presentation.components.UpdateDialogHost
import com.example.presentation.components.XpStartMenu
import com.example.presentation.components.XpTaskbar
import com.example.presentation.screens.ProjectsScreen
import com.example.presentation.screens.SettingsScreen
import com.example.presentation.screens.SplashScreen
import com.example.presentation.screens.WorkScreen
import com.example.presentation.viewmodel.MainViewModel
import com.example.ui.theme.SpriteXPTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.uiState.collectAsState()
            val projects by viewModel.projectsFlow.collectAsState(initial = emptyList())

            val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) viewModel.importVideo(uri)
            }
            val sheetPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) viewModel.importSpriteSheet(uri)
            }

            UpdateDialogHost(viewModel = viewModel)

            SpriteXPTheme(darkTheme = state.isDarkTheme) {
                BackHandler(enabled = state.currentScreen != "work" && state.currentScreen != "splash") {
                    if (state.isStartMenuOpen) {
                        viewModel.toggleStartMenu()
                    } else {
                        viewModel.navigateTo("work")
                    }
                }

                if (state.currentScreen == "splash") {
                    SplashScreen(progress = state.splashProgress)
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            XpTaskbar(
                                isStartMenuOpen = state.isStartMenuOpen,
                                onStartClick = { viewModel.toggleStartMenu() },
                                activeScreenName = when (state.currentScreen) {
                                    "work" -> "Trabalho"
                                    "projects" -> "Meus Projetos"
                                    "settings" -> "Configurações"
                                    else -> "Trabalho"
                                },
                                onActiveTaskClick = {
                                    if (state.currentScreen != "work") {
                                        viewModel.navigateTo("work")
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (state.currentScreen) {
                                "work" -> {
                                    WorkScreen(
                                        settings = state.pixelSettings,
                                        onSettingsChange = { t -> viewModel.updatePixelSettings(t) },
                                        originalFrame = viewModel.currentRawFrame(),
                                        convertedFrame = viewModel.currentConvertedFrame(),
                                        isConverting = state.isConverting,
                                        frames = state.videoFrames,
                                        currentFrameIndex = state.currentFrameIndex,
                                        frameCount = state.videoFrames.size.coerceAtLeast(1),
                                        isPlaying = state.isPlayingPreview,
                                        fps = state.videoFps,
                                        startFrame = state.videoStartFrame,
                                        endFrame = state.videoEndFrame,
                                        isImporting = state.isImporting,
                                        onPickVideo = { videoPicker.launch("video/*") },
                                        onTogglePlay = { viewModel.toggleVideoPlay() },
                                        onFpsChange = { viewModel.setVideoFps(it) },
                                        onIntervalChange = { s, e -> viewModel.setVideoInterval(s, e) },
                                        onSelectFrame = { viewModel.selectVideoFrame(it) },
                                        onExportSheet = {
                                            viewModel.setExportFormat(ExportFormat.SPRITE_SHEET_PNG)
                                            viewModel.executeExport()
                                        },
                                        onExportZip = {
                                            viewModel.setExportFormat(ExportFormat.ZIP_PNGS)
                                            viewModel.executeExport()
                                        },
                                        isExporting = state.isExporting,
                                        exportProgress = state.exportProgress,
                                        analyzerSheet = state.analyzerSheetBitmap,
                                        analyzerColumns = state.analyzerColumns,
                                        analyzerRows = state.analyzerRows,
                                        analyzerStart = state.analyzerSelectedRangeStart,
                                        analyzerEnd = state.analyzerSelectedRangeEnd,
                                        analyzerFps = state.analyzerFps,
                                        onAnalyzerRange = { s, e -> viewModel.setAnalyzerRange(s, e) },
                                        onPickSheet = { sheetPicker.launch("image/*") },
                                        onNavigateProjects = { viewModel.navigateTo("projects") },
                                        onNavigateSettings = { viewModel.navigateTo("settings") },
                                        onCheckUpdates = { viewModel.checkForUpdates(manual = true) },
                                        onShowAbout = {
                                            viewModel.showAlert(
                                                "SpriteXP v1.0\nOficina de Pixel Art Retrô\nArquitetura Limpa\nAndroid 8.0+"
                                            )
                                        }
                                    )
                                }
                                "projects" -> ProjectsScreen(
                                    projects = projects,
                                    onNewProject = { viewModel.navigateTo("work") },
                                    onOpenProject = { _ -> viewModel.navigateTo("work") },
                                    onDeleteProject = { id -> viewModel.deleteProject(id) },
                                    onCloseClick = { viewModel.navigateTo("work") }
                                )
                                "settings" -> SettingsScreen(
                                    isDarkTheme = state.isDarkTheme,
                                    soundEnabled = state.soundEnabled,
                                    language = state.language,
                                    onDarkThemeChange = { viewModel.setDarkTheme(it) },
                                    onSoundChange = { viewModel.setSoundEnabled(it) },
                                    onLanguageChange = { viewModel.setLanguage(it) },
                                    onShowAbout = {
                                        viewModel.showAlert(
                                            "SpriteXP v1.0\nOficina de Pixel Art Retrô\nArquitetura Limpa\nAndroid 8.0+"
                                        )
                                    },
                                    onCheckUpdates = { viewModel.checkForUpdates(manual = true) },
                                    onCloseClick = { viewModel.navigateTo("work") }
                                )
                                else -> viewModel.navigateTo("work")
                            }

                            if (state.isStartMenuOpen) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .clickable { viewModel.toggleStartMenu() }
                                ) {
                                    XpStartMenu(
                                        isOpen = true,
                                        onDismiss = { viewModel.toggleStartMenu() },
                                        onNavigate = { screen ->
                                            val target = when (screen) {
                                                "desktop", "converter", "video", "analyzer",
                                                "palette", "export" -> "work"
                                                else -> screen
                                            }
                                            viewModel.navigateTo(target)
                                        },
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(bottom = 2.dp)
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
