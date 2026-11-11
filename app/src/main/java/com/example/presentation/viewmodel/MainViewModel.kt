package com.example.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.pipeline.MediaProcessingPipeline
import com.example.data.pipeline.PixelArtConverterPipeline
import com.example.data.repository.InMemoryProjectRepository
import com.example.data.repository.ProjectRepository
import com.example.domain.model.DitherMode
import com.example.domain.model.ExportFormat
import com.example.domain.model.PalettePreset
import com.example.domain.model.PixelArtSettings
import com.example.domain.model.SpriteProject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class MainUiState(
    val currentScreen: String = "splash", // splash, desktop, converter, video, analyzer, palette, export, projects, settings
    val splashProgress: Float = 0.6f,
    val isStartMenuOpen: Boolean = false,
    val isDarkTheme: Boolean = false,
    val soundEnabled: Boolean = true,
    val language: String = "Português",

    // Conversor Imagem
    val originalBitmap: Bitmap? = null,
    val convertedBitmap: Bitmap? = null,
    val isConverting: Boolean = false,
    val pixelSettings: PixelArtSettings = PixelArtSettings(),

    // Vídeo e Sprite Sheet
    val videoFrames: List<Bitmap> = emptyList(),
    val videoStartFrame: Int = 1,
    val videoEndFrame: Int = 12,
    val currentFrameIndex: Int = 0,
    val isPlayingPreview: Boolean = false,
    val videoFps: Int = 8,

    // Analisador de Sprite Sheet
    val analyzerSheetBitmap: Bitmap? = null,
    val analyzerColumns: Int = 6,
    val analyzerRows: Int = 6,
    val analyzerSelectedRangeStart: Int = 1,
    val analyzerSelectedRangeEnd: Int = 6,
    val analyzerFps: Int = 8,

    // Exportação
    val exportFormat: ExportFormat = ExportFormat.SPRITE_SHEET_PNG,
    val exportProgress: Float = 0f,
    val isExporting: Boolean = false,
    val exportedFilePath: String? = null,

    // Mensagens de Alerta / Diálogo XP
    val alertMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val converterPipeline = PixelArtConverterPipeline()
    private val mediaPipeline = MediaProcessingPipeline(application)
    val projectRepository: ProjectRepository = InMemoryProjectRepository()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val projectsFlow = projectRepository.getProjects()

    private var debounceConversionJob: Job? = null
    private var animationJob: Job? = null

    init {
        createProceduralInitialBitmaps()
        startSplashLoading()
    }

    /**
     * Gera dinamicamente bitmaps funcionais em memória via Canvas puro,
     * sem nenhuma imagem estática mock acoplada aos assets.
     */
    private fun createProceduralInitialBitmaps() {
        try {
            // Cria um bitmap procedural funcional de 64x64 para o conversor
            val origBmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(origBmp)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Fundo verde Chroma Key
            paint.color = android.graphics.Color.rgb(0, 255, 0)
            canvas.drawRect(0f, 0f, 64f, 64f, paint)

            // Corpo do personagem central
            paint.color = android.graphics.Color.rgb(36, 94, 220)
            canvas.drawRect(20f, 24f, 44f, 52f, paint)

            // Cabeça
            paint.color = android.graphics.Color.rgb(255, 204, 170)
            canvas.drawCircle(32f, 18f, 10f, paint)

            // Chapéu pontudo
            paint.color = android.graphics.Color.rgb(10, 36, 106)
            canvas.drawRect(24f, 6f, 40f, 12f, paint)

            // Olhos
            paint.color = android.graphics.Color.BLACK
            canvas.drawCircle(29f, 18f, 1.5f, paint)
            canvas.drawCircle(35f, 18f, 1.5f, paint)

            // Cria uma folha de sprite sheet procedural (6 colunas x 6 linhas)
            val sheetCols = 6
            val sheetRows = 6
            val frameSize = 32
            val sheetBmp = Bitmap.createBitmap(sheetCols * frameSize, sheetRows * frameSize, Bitmap.Config.ARGB_8888)
            val sheetCanvas = Canvas(sheetBmp)

            val dummyFrames = mutableListOf<Bitmap>()
            val colors = listOf(
                android.graphics.Color.rgb(36, 94, 220),
                android.graphics.Color.rgb(224, 87, 42),
                android.graphics.Color.rgb(60, 154, 60),
                android.graphics.Color.rgb(126, 37, 83),
                android.graphics.Color.rgb(255, 163, 0),
                android.graphics.Color.rgb(41, 173, 255)
            )

            for (i in 0 until (sheetCols * sheetRows)) {
                val col = i % sheetCols
                val row = i / sheetCols
                val left = (col * frameSize).toFloat()
                val top = (row * frameSize).toFloat()

                // Fundo transparente com borda de grade
                paint.color = android.graphics.Color.TRANSPARENT
                sheetCanvas.drawRect(left, top, left + frameSize, top + frameSize, paint)

                // Figura geométrica animada com variação de posição
                val offset = (i % 6) * 2f
                paint.color = colors[i % colors.size]
                sheetCanvas.drawCircle(left + frameSize / 2f, top + frameSize / 2f - offset + 4f, 8f, paint)

                // Extrai os primeiros 12 frames para o módulo de vídeo
                if (i < 12) {
                    val singleFrame = Bitmap.createBitmap(frameSize, frameSize, Bitmap.Config.ARGB_8888)
                    val sCanvas = Canvas(singleFrame)
                    paint.color = colors[i % colors.size]
                    sCanvas.drawCircle(frameSize / 2f, frameSize / 2f - offset + 4f, 8f, paint)
                    dummyFrames.add(singleFrame)
                }
            }

            _uiState.value = _uiState.value.copy(
                originalBitmap = origBmp,
                analyzerSheetBitmap = sheetBmp,
                videoFrames = dummyFrames
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startSplashLoading() {
        viewModelScope.launch {
            for (p in 10..100 step 10) {
                delay(100)
                _uiState.value = _uiState.value.copy(splashProgress = p / 100f)
            }
            delay(150)
            _uiState.value = _uiState.value.copy(
                currentScreen = "desktop",
                alertMessage = "Bem-vindo ao SpriteXP!"
            )
            requestPixelConversion()
        }
    }

    fun navigateTo(screen: String) {
        _uiState.value = _uiState.value.copy(
            currentScreen = screen,
            isStartMenuOpen = false
        )
    }

    fun toggleStartMenu() {
        _uiState.value = _uiState.value.copy(isStartMenuOpen = !_uiState.value.isStartMenuOpen)
    }

    fun dismissAlert() {
        _uiState.value = _uiState.value.copy(alertMessage = null)
    }

    fun showAlert(msg: String) {
        _uiState.value = _uiState.value.copy(alertMessage = msg)
    }

    fun setDarkTheme(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkTheme = enabled)
    }

    fun setSoundEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(soundEnabled = enabled)
    }

    fun setLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(language = lang)
    }

    fun updatePixelSettings(transform: (PixelArtSettings) -> PixelArtSettings) {
        val newSettings = transform(_uiState.value.pixelSettings)
        _uiState.value = _uiState.value.copy(pixelSettings = newSettings)
        scheduleDebouncedConversion()
    }

    private fun scheduleDebouncedConversion() {
        debounceConversionJob?.cancel()
        debounceConversionJob = viewModelScope.launch {
            delay(120)
            requestPixelConversion()
        }
    }

    fun requestPixelConversion() {
        val original = _uiState.value.originalBitmap ?: return
        val settings = _uiState.value.pixelSettings
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConverting = true)
            val paletteColors = when (settings.palettePreset) {
                PalettePreset.PICO_8 -> PalettePreset.PICO_8.colors.map { it.toArgb() }
                PalettePreset.SWEETIE_16 -> PalettePreset.SWEETIE_16.colors.map { it.toArgb() }
                PalettePreset.ENDESGA_32 -> PalettePreset.ENDESGA_32.colors.map { it.toArgb() }
                PalettePreset.GAME_BOY -> PalettePreset.GAME_BOY.colors.map { it.toArgb() }
                PalettePreset.AUTO_EXTRACT -> emptyList()
            }

            val converted = converterPipeline.convert(
                source = original,
                targetWidth = settings.targetResolution,
                targetHeight = settings.targetResolution,
                chromaKeyEnabled = settings.chromaKeyEnabled,
                chromaColorInt = settings.chromaKeyColor.toArgb(),
                chromaTolerance = settings.chromaTolerance,
                contrast = settings.contrast,
                brightness = settings.brightness,
                edgeEnhance = settings.edgeEnhance,
                paletteColorsInt = paletteColors,
                ditherModeName = settings.ditherMode.name
            )

            _uiState.value = _uiState.value.copy(
                convertedBitmap = converted,
                isConverting = false
            )
        }
    }

    fun setOriginalImage(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(originalBitmap = bitmap)
        requestPixelConversion()
    }

    // Controle de Animação de Vídeo & Timeline
    fun setVideoInterval(start: Int, end: Int) {
        _uiState.value = _uiState.value.copy(
            videoStartFrame = maxOf(1, start),
            videoEndFrame = maxOf(start, end)
        )
    }

    fun setVideoFps(fps: Int) {
        _uiState.value = _uiState.value.copy(videoFps = maxOf(1, minOf(30, fps)))
    }

    fun selectVideoFrame(index: Int) {
        _uiState.value = _uiState.value.copy(currentFrameIndex = index)
    }

    fun toggleVideoPlay() {
        val playing = !_uiState.value.isPlayingPreview
        _uiState.value = _uiState.value.copy(isPlayingPreview = playing)
        if (playing) {
            startVideoPlaybackLoop()
        } else {
            animationJob?.cancel()
        }
    }

    private fun startVideoPlaybackLoop() {
        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            while (_uiState.value.isPlayingPreview) {
                val frames = _uiState.value.videoFrames
                if (frames.isNotEmpty()) {
                    val next = (_uiState.value.currentFrameIndex + 1) % frames.size
                    _uiState.value = _uiState.value.copy(currentFrameIndex = next)
                }
                delay((1000L / maxOf(1, _uiState.value.videoFps)))
            }
        }
    }

    // Analisador de Sprite Sheet
    fun setAnalyzerRange(start: Int, end: Int) {
        _uiState.value = _uiState.value.copy(
            analyzerSelectedRangeStart = start,
            analyzerSelectedRangeEnd = end
        )
    }

    fun setAnalyzerGrid(cols: Int, rows: Int) {
        _uiState.value = _uiState.value.copy(
            analyzerColumns = maxOf(1, cols),
            analyzerRows = maxOf(1, rows)
        )
    }

    // Exportação
    fun setExportFormat(format: ExportFormat) {
        _uiState.value = _uiState.value.copy(exportFormat = format)
    }

    fun executeExport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportProgress = 0.1f)
            delay(100)
            _uiState.value = _uiState.value.copy(exportProgress = 0.4f)
            delay(150)

            val frames = _uiState.value.videoFrames.ifEmpty {
                listOfNotNull(_uiState.value.convertedBitmap ?: _uiState.value.originalBitmap)
            }

            var exportedFile: File? = null
            when (_uiState.value.exportFormat) {
                ExportFormat.SPRITE_SHEET_PNG -> {
                    val sheet = mediaPipeline.createMergedSpriteSheet(frames, 4)
                    val file = File(getApplication<Application>().cacheDir, "SpriteXP_Sheet.png")
                    file.outputStream().use { sheet.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    exportedFile = file
                }
                ExportFormat.ZIP_PNGS -> {
                    exportedFile = mediaPipeline.exportZip(frames, "SpriteXP_Frames")
                }
                ExportFormat.JSON_METADATA -> {
                    exportedFile = mediaPipeline.exportJsonMetadata(
                        projectName = "SpriteXP_Project",
                        frameWidth = 64,
                        frameHeight = 64,
                        columns = 4,
                        rows = (frames.size + 3) / 4,
                        frameCount = frames.size,
                        fps = _uiState.value.videoFps
                    )
                }
            }

            _uiState.value = _uiState.value.copy(exportProgress = 0.85f)
            delay(100)
            _uiState.value = _uiState.value.copy(
                exportProgress = 1.0f,
                isExporting = false,
                exportedFilePath = exportedFile?.absolutePath,
                alertMessage = "Exportação concluída com sucesso! Arquivo gerado: ${exportedFile?.name}"
            )

            // Salva projeto na biblioteca com categoria de ícone Material
            projectRepository.addProject(
                SpriteProject(
                    id = System.currentTimeMillis().toString(),
                    title = "Projeto Sprite " + (frames.size) + "f",
                    dateString = "Hoje",
                    frameCount = frames.size,
                    resolution = 64,
                    paletteName = _uiState.value.pixelSettings.palettePreset.displayName,
                    iconCategory = "brush"
                )
            )
        }
    }
}
