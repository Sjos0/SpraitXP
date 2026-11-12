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
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.presentation.components.XpStartMenu
import com.example.presentation.components.XpTaskbar
import com.example.presentation.screens.AnalyzerScreen
import com.example.presentation.screens.ConverterScreen
import com.example.presentation.screens.DesktopScreen
import com.example.presentation.screens.ExportScreen
import com.example.presentation.screens.PaletteScreen
import com.example.presentation.screens.ProjectsScreen
import com.example.presentation.screens.SettingsScreen
import com.example.presentation.screens.SplashScreen
import com.example.presentation.screens.VideoScreen
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
                // Manipulador de botão voltar do Android (BackHandler)
                BackHandler(enabled = state.currentScreen != "desktop" && state.currentScreen != "splash") {
                    if (state.isStartMenuOpen) {
                        viewModel.toggleStartMenu()
                    } else {
                        viewModel.navigateTo("desktop")
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
                                    "converter" -> "Conversor"
                                    "video" -> "Vídeo"
                                    "analyzer" -> "Analisador"
                                    "palette" -> "Paleta"
                                    "export" -> "Exportar"
                                    "projects" -> "Meus Projetos"
                                    "settings" -> "Configurações"
                                    else -> "Área de Trabalho"
                                },
                                onActiveTaskClick = {
                                    // Alterna ou minimiza
                                    if (state.currentScreen != "desktop") {
                                        viewModel.navigateTo("desktop")
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
                            // Renderiza a Área de Trabalho base
                            DesktopScreen(
                                alertMessage = state.alertMessage,
                                onDismissAlert = { viewModel.dismissAlert() },
                                onNavigate = { screen -> viewModel.navigateTo(screen) }
                            )

                            // Renderiza a Janela da Aplicação Aberta com transição
                            if (state.currentScreen != "desktop") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0x33000000))
                                ) {
                                    when (state.currentScreen) {
                                        "converter" -> {
                                            ConverterScreen(
                                                originalBitmap = state.originalBitmap,
                                                convertedBitmap = state.convertedBitmap,
                                                isConverting = state.isConverting,
                                                settings = state.pixelSettings,
                                                onSettingsChange = { transform -> viewModel.updatePixelSettings(transform) },
                                                onImagePicked = { bmp -> viewModel.setOriginalImage(bmp) },
                                                onExportClick = { viewModel.navigateTo("export") },
                                                onCloseClick = { viewModel.navigateTo("desktop") },
                                                frameCount = state.videoFrames.size.coerceAtLeast(1),
                                                fps = state.videoFps,
                                                onFpsChange = { f -> viewModel.setVideoFps(f) },
                                                onQuickPlay = { viewModel.toggleVideoPlay() },
                                                onRenderPlay = { viewModel.toggleVideoPlay() }
                                            )
                                        }
                                        "video" -> {
                                            VideoScreen(
                                                frames = state.videoFrames,
                                                startFrame = state.videoStartFrame,
                                                endFrame = state.videoEndFrame,
                                                currentFrameIndex = state.currentFrameIndex,
                                                isPlaying = state.isPlayingPreview,
                                                fps = state.videoFps,
                                                onIntervalChange = { s: Int, e: Int -> viewModel.setVideoInterval(s, e) },
                                                onFpsChange = { fps: Int -> viewModel.setVideoFps(fps) },
                                                onSelectFrame = { idx: Int -> viewModel.selectVideoFrame(idx) },
                                                onTogglePlay = { viewModel.toggleVideoPlay() },
                                                onPickVideo = { videoPicker.launch("video/*") },
                                                isImporting = state.isImporting,
                                                onExportClick = { viewModel.navigateTo("export") },
                                                onCloseClick = { viewModel.navigateTo("desktop") }
                                            )
                                        }
                                        "analyzer" -> {
                                            AnalyzerScreen(
                                                sheetBitmap = state.analyzerSheetBitmap,
                                                columns = state.analyzerColumns,
                                                rows = state.analyzerRows,
                                                startRange = state.analyzerSelectedRangeStart,
                                                endRange = state.analyzerSelectedRangeEnd,
                                                fps = state.analyzerFps,
                                                onRangeChange = { s, e -> viewModel.setAnalyzerRange(s, e) },
                                                onPickSheet = { sheetPicker.launch("image/*") },
                                                onExportClick = { viewModel.navigateTo("export") },
                                                onCloseClick = { viewModel.navigateTo("desktop") }
                                            )
                                        }
                                        "palette" -> {
                                            PaletteScreen(
                                                settings = state.pixelSettings,
                                                previewBitmap = state.convertedBitmap ?: state.originalBitmap,
                                                onSettingsChange = { transform -> viewModel.updatePixelSettings(transform) },
                                                onApply = {
                                                    viewModel.requestPixelConversion()
                                                    viewModel.navigateTo("converter")
                                                },
                                                onCloseClick = { viewModel.navigateTo("desktop") }
                                            )
                                        }
                                        "projects" -> {
                                            ProjectsScreen(
                                                projects = projects,
                                                onNewProject = { viewModel.navigateTo("converter") },
                                                onOpenProject = { _ -> viewModel.navigateTo("analyzer") },
                                                onDeleteProject = { id -> viewModel.deleteProject(id) },
                                                onCloseClick = { viewModel.navigateTo("desktop") }
                                            )
                                        }
                                        "export" -> {
                                            ExportScreen(
                                                currentFormat = state.exportFormat,
                                                isExporting = state.isExporting,
                                                progress = state.exportProgress,
                                                onFormatChange = { fmt -> viewModel.setExportFormat(fmt) },
                                                onExportClick = { viewModel.executeExport() },
                                                onCloseClick = { viewModel.navigateTo("desktop") }
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
                                                    viewModel.showAlert("SpriteXP v1.0\nOficina de Pixel Art Retrô\nArquitetura Limpa (Clean Architecture)\nCompatível com Android 8.0+")
                                                },
                                                onCheckUpdates = { viewModel.checkForUpdates(manual = true) },
                                                onCloseClick = { viewModel.navigateTo("desktop") }
                                            )
                                        }
                                    }
                                }
                            }

                            // Menu Iniciar Flutuante
                            if (state.isStartMenuOpen) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { viewModel.toggleStartMenu() }
                                ) {
                                    XpStartMenu(
                                        isOpen = true,
                                        onDismiss = { viewModel.toggleStartMenu() },
                                        onNavigate = { screen -> viewModel.navigateTo(screen) },
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
