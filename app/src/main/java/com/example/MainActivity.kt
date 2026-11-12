package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.presentation.components.UpdateDialogHost
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.presentation.components.XpStartMenu
import com.example.presentation.components.XpTaskbar
import com.example.presentation.screens.ProjectsScreen
import com.example.presentation.screens.SettingsScreen
import com.example.presentation.screens.SplashScreen
import com.example.presentation.screens.WorkspaceScreen
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

            val multiImagePicker = rememberLauncherForActivityResult(
                ActivityResultContracts.GetMultipleContents()
            ) { uris ->
                if (uris.isNotEmpty()) viewModel.importImages(uris)
            }
            val sheetPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) viewModel.importSpriteSheet(uri)
            }

            UpdateDialogHost(viewModel = viewModel)

            SpriteXPTheme(darkTheme = state.isDarkTheme) {
                BackHandler(enabled = state.currentScreen != "workspace" && state.currentScreen != "splash") {
                    if (state.isStartMenuOpen) {
                        viewModel.toggleStartMenu()
                    } else {
                        viewModel.navigateTo("workspace")
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
                                    "workspace" -> "Área de trabalho"
                                    "projects" -> "Meus Projetos"
                                    "settings" -> "Configurações"
                                    else -> "Área de trabalho"
                                },
                                onActiveTaskClick = {
                                    if (state.currentScreen != "workspace") {
                                        viewModel.navigateTo("workspace")
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
                                "workspace" -> {
                                    WorkspaceScreen(
                                        originalBitmap = state.originalBitmap,
                                        convertedBitmap = state.convertedBitmap,
                                        isConverting = state.isConverting,
                                        settings = state.pixelSettings,
                                        onSettingsChange = { transform -> viewModel.updatePixelSettings(transform) },
                                        onImagePicked = { bmp -> viewModel.setOriginalImage(bmp) },
                                        onImportMultipleImages = { multiImagePicker.launch("image/*") },
                                        frames = state.videoFrames,
                                        startFrame = state.videoStartFrame,
                                        endFrame = state.videoEndFrame,
                                        currentFrameIndex = state.currentFrameIndex,
                                        isPlaying = state.isPlayingPreview,
                                        fps = state.videoFps,
                                        onIntervalChange = { s, e -> viewModel.setVideoInterval(s, e) },
                                        onFpsChange = { f -> viewModel.setVideoFps(f) },
                                        onSelectFrame = { idx -> viewModel.selectVideoFrame(idx) },
                                        onTogglePlay = { viewModel.toggleVideoPlay() },
                                        isImporting = state.isImporting,
                                        sheetBitmap = state.analyzerSheetBitmap,
                                        columns = state.analyzerColumns,
                                        rows = state.analyzerRows,
                                        startRange = state.analyzerSelectedRangeStart,
                                        endRange = state.analyzerSelectedRangeEnd,
                                        analyzerFps = state.analyzerFps,
                                        onRangeChange = { s, e -> viewModel.setAnalyzerRange(s, e) },
                                        onPickSheet = { sheetPicker.launch("image/*") },
                                        currentFormat = state.exportFormat,
                                        isExporting = state.isExporting,
                                        exportProgress = state.exportProgress,
                                        onFormatChange = { fmt -> viewModel.setExportFormat(fmt) },
                                        onExportClick = { viewModel.executeExport() },
                                        onNavigateProjects = { viewModel.navigateTo("projects") },
                                        onNavigateSettings = { viewModel.navigateTo("settings") },
                                        onCheckUpdates = { viewModel.checkForUpdates(manual = true) },
                                        onShowAbout = {
                                            viewModel.showAlert(
                                                "SpriteXP v1.0\nOficina de Pixel Art Retrô\nArquitetura Limpa (Clean Architecture)\nCompatível com Android 8.0+"
                                            )
                                        }
                                    )
                                }
                                "projects" -> {
                                    ProjectsScreen(
                                        projects = projects,
                                        onNewProject = { viewModel.navigateTo("workspace") },
                                        onOpenProject = { _ -> viewModel.navigateTo("workspace") },
                                        onDeleteProject = { id -> viewModel.deleteProject(id) },
                                        onCloseClick = { viewModel.navigateTo("workspace") }
                                    )
                                }
                                "settings" -> {
                                    SettingsScreen(
                                        isDarkTheme = state.isDarkTheme,
                                        soundEnabled = state.soundEnabled,
                                        language = state.language,
                                        onDarkThemeChange = { d -> viewModel.setDarkTheme(d) },
                                        onSoundChange = { s -> viewModel.setSoundEnabled(s) },
                                        onLanguageChange = { l -> viewModel.setLanguage(l) },
                                        onShowAbout = {
                                            viewModel.showAlert(
                                                "SpriteXP v1.0\nOficina de Pixel Art Retrô\nArquitetura Limpa (Clean Architecture)\nCompatível com Android 8.0+"
                                            )
                                        },
                                        onCheckUpdates = { viewModel.checkForUpdates(manual = true) },
                                        onCloseClick = { viewModel.navigateTo("workspace") }
                                    )
                                }
                                else -> {
                                    viewModel.navigateTo("workspace")
                                }
                            }

                            if (state.isStartMenuOpen) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { viewModel.toggleStartMenu() }
                                ) {
                                    XpStartMenu(
                                        isOpen = true,
                                        onDismiss = { viewModel.toggleStartMenu() },
                                        onNavigate = { screen ->
                                            val target = when (screen) {
                                                "desktop", "converter", "video", "analyzer",
                                                "palette", "export" -> "workspace"
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
