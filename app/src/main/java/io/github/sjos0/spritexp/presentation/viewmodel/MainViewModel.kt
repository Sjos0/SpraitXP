package io.github.sjos0.spritexp.presentation.viewmodel

import io.github.sjos0.spritexp.domain.model.SettingsPreset
import io.github.sjos0.spritexp.data.repository.PresetRepository
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.sjos0.spritexp.BuildConfig
import io.github.sjos0.spritexp.data.pipeline.MediaProcessingPipeline
import io.github.sjos0.spritexp.data.storage.SpriteXpStorage
import io.github.sjos0.spritexp.data.update.AppUpdater
import io.github.sjos0.spritexp.data.update.InstallEvent
import io.github.sjos0.spritexp.data.update.InstallEvents
import io.github.sjos0.spritexp.data.update.ReleaseInfo
import io.github.sjos0.spritexp.data.update.UpdateStage
import io.github.sjos0.spritexp.data.update.UpdateUiState
import io.github.sjos0.spritexp.data.pipeline.PaletteExtractor
import io.github.sjos0.spritexp.data.pipeline.PixelArtConverterPipeline
import io.github.sjos0.spritexp.data.repository.JsonProjectRepository
import io.github.sjos0.spritexp.data.repository.ProjectRepository
import io.github.sjos0.spritexp.domain.model.ExportFormat
import io.github.sjos0.spritexp.domain.model.PalettePreset
import io.github.sjos0.spritexp.domain.model.PixelArtSettings
import io.github.sjos0.spritexp.domain.model.SpriteProject
import io.github.sjos0.spritexp.domain.usecase.ImageRules
import io.github.sjos0.spritexp.domain.usecase.VideoFrameMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.sqrt

/** De onde vieram os quadros atuais. Define o que a exportação usa. */
enum class SourceKind { DEMO, IMAGE, VIDEO, SHEET }

/**
 * Quantos quadros o export vai gerar, sem montar os bitmaps (usado na UI).
 * Imagem convertida = 1 quadro; vídeo = quadros do intervalo; sheet = quadros selecionados.
 */
val MainUiState.exportFrameCount: Int
    get() = when (sourceKind) {
        SourceKind.VIDEO -> videoFrames.size
        SourceKind.IMAGE, SourceKind.DEMO -> if (convertedBitmap != null) 1 else 0
        SourceKind.SHEET -> if (analyzerSheetBitmap == null) 0
            else (analyzerSelectedRangeEnd - analyzerSelectedRangeStart + 1).coerceIn(0, analyzerColumns * analyzerRows)
    }

data class MainUiState(
    val currentScreen: String = "splash", // splash, desktop, converter, video, analyzer, palette, export, projects, settings
    val splashProgress: Float = 0.6f,
    val isStartMenuOpen: Boolean = false,
    val isDarkTheme: Boolean = true,
    val soundEnabled: Boolean = true,
    val language: String = "Português",

    // Origem atual
    val sourceKind: SourceKind = SourceKind.DEMO,
    val sourceName: String = "",
    val isImporting: Boolean = false,
    val importProgress: Float = 0f,

    // Conversor Imagem
    val originalBitmap: Bitmap? = null,
    val convertedBitmap: Bitmap? = null,
    val isConverting: Boolean = false,
    val pixelSettings: PixelArtSettings = PixelArtSettings(),

    // Vídeo e Sprite Sheet
    val videoFrames: List<Bitmap> = emptyList(),
    val videoStartFrame: Int = 1,
    val videoEndFrame: Int = 1,
    val videoTotalFrames: Int = 0,
    val videoSourceFps: Int = VideoFrameMath.DEFAULT_FPS,
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

    private val tag = "SpriteXP"
    private val converterPipeline = PixelArtConverterPipeline()
    private val mediaPipeline = MediaProcessingPipeline(application)
    val projectRepository: ProjectRepository = JsonProjectRepository(application)

    private val presetRepository = PresetRepository(application)
    private val _presets = MutableStateFlow<List<SettingsPreset>>(emptyList())

    /** Presets salvos em Download/SpriteXP/presets.json. */
    val presets: StateFlow<List<SettingsPreset>> = _presets.asStateFlow()

    init {
        viewModelScope.launch {
            _presets.value = withContext(Dispatchers.IO) { presetRepository.load() }
        }
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val updater = AppUpdater(application)
    private val _updateState = MutableStateFlow(UpdateUiState())
    val updateState: StateFlow<UpdateUiState> = _updateState.asStateFlow()

    val projectsFlow = projectRepository.getProjects()

    private var debounceConversionJob: Job? = null
    private var conversionJob: Job? = null
    private var videoJob: Job? = null
    private var animationJob: Job? = null

    /** Cada pedido de conversão recebe um número; só o mais recente pode alterar a tela. */
    private var conversionVersion = 0L

    /** Quadros originais do vídeo (já reduzidos), antes da conversão para pixel art. */
    private var rawVideoFrames: List<Bitmap> = emptyList()

    /** Intervalo da origem (1 based, inclusivo) que os quadros de [rawVideoFrames] representam. */
    private var rawRangeStart: Int = 1
    private var rawRangeEnd: Int = 0

    /** Verdadeiro enquanto uma extração de quadros está em andamento. */
    private var extractInFlight: Boolean = false
    private var videoUri: String? = null

    init {
        observeInstallEvents()
        startSplashLoading()
    }

    private fun startSplashLoading() {
        viewModelScope.launch {
            for (p in 10..100 step 10) {
                delay(100)
                _uiState.value = _uiState.value.copy(splashProgress = p / 100f)
            }
            delay(150)
            _uiState.value = _uiState.value.copy(
                currentScreen = "work",
                alertMessage = "Bem-vindo ao SpriteXP!"
            )
            requestPixelConversion()
            announceIfJustUpdated()
            checkForUpdates(manual = false)
        }
    }

    // ---------- Navegação e configurações ----------

    fun navigateTo(screen: String) {
        _uiState.value = _uiState.value.copy(currentScreen = screen, isStartMenuOpen = false)
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

    // ---------- Conversão para pixel art ----------

    /**
     * Converte a imagem atual (e os quadros do vídeo, se houver).
     * Um pedido novo cancela o anterior, e só o resultado mais recente é aplicado.
     */
    fun requestPixelConversion() {
        val snapshot = _uiState.value
        val original = snapshot.originalBitmap
        val frames = if (snapshot.sourceKind == SourceKind.VIDEO) activeRawFrames(snapshot) else emptyList()
        if (original == null && frames.isEmpty()) return

        val settings = snapshot.pixelSettings
        val version = ++conversionVersion
        conversionJob?.cancel()
        conversionJob = viewModelScope.launch {
            _uiState.update { it.copy(isConverting = true) }
            try {
                val palette = resolvePalette(frames.firstOrNull() ?: original, settings)
                val convertedFrames = if (snapshot.sourceKind == SourceKind.VIDEO && frames.isNotEmpty()) {
                    frames.map { convertOne(it, settings, palette) }
                } else {
                    emptyList()
                }
                val converted = original?.let { convertOne(it, settings, palette) }
                val videoSheet = if (convertedFrames.isNotEmpty()) buildVideoSheet(convertedFrames) else null

                // Ignora resultado de um pedido que já foi substituído.
                if (version == conversionVersion) {
                    _uiState.update { state ->
                        state.copy(
                            convertedBitmap = converted ?: state.convertedBitmap,
                            videoFrames = if (convertedFrames.isNotEmpty()) convertedFrames else state.videoFrames,
                            analyzerSheetBitmap = videoSheet?.bitmap ?: state.analyzerSheetBitmap,
                            analyzerColumns = videoSheet?.columns ?: state.analyzerColumns,
                            analyzerRows = videoSheet?.rows ?: state.analyzerRows,
                            analyzerSelectedRangeStart = if (videoSheet != null) 1 else state.analyzerSelectedRangeStart,
                            analyzerSelectedRangeEnd = if (videoSheet != null) convertedFrames.size else state.analyzerSelectedRangeEnd
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e(tag, "Falha na conversão", e)
                if (version == conversionVersion) {
                    showAlert("Não foi possível converter. Ajuste as configurações e tente de novo.")
                }
            } finally {
                // A prévia nunca fica presa em "Convertendo..." (sucesso, erro ou cancelamento).
                if (version == conversionVersion) _uiState.update { it.copy(isConverting = false) }
            }
        }
    }

    /** Salva os ajustes atuais (Ajustes, Paleta e FPS) como um preset novo. */
    fun savePreset(rawName: String) {
        val name = rawName.trim().take(40)
        if (name.isEmpty()) {
            showAlert("Dê um nome ao preset.")
            return
        }
        val s = _uiState.value
        val preset = SettingsPreset(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            createdAt = System.currentTimeMillis(),
            settings = s.pixelSettings,
            videoFps = s.videoFps
        )
        persistPresets(_presets.value + preset, "Preset \"$name\" salvo.")
    }

    /** Troca os ajustes de um preset pelos atuais. */
    fun overwritePreset(id: String) {
        val s = _uiState.value
        val updated = _presets.value.map {
            if (it.id == id) it.copy(settings = s.pixelSettings, videoFps = s.videoFps, createdAt = System.currentTimeMillis()) else it
        }
        persistPresets(updated, "Preset atualizado com os ajustes atuais.")
    }

    fun deletePreset(id: String) {
        persistPresets(_presets.value.filterNot { it.id == id }, "Preset excluído.")
    }

    /** Aplica settings e FPS do preset e refaz a conversão. Não reimporta o vídeo. */
    fun applyPreset(id: String) {
        val preset = _presets.value.firstOrNull { it.id == id } ?: return
        _uiState.update { it.copy(pixelSettings = preset.settings, videoFps = preset.videoFps.coerceIn(1, 60)) }
        scheduleDebouncedConversion()
        showAlert("Preset \"${preset.name}\" aplicado.")
    }

    private fun persistPresets(list: List<SettingsPreset>, okMessage: String) {
        _presets.value = list
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) { presetRepository.save(list) }
            showAlert(if (saved) okMessage else "Não foi possível salvar os presets em ${SpriteXpStorage.DISPLAY_DIR}.")
        }
    }

    /** Sprite sheet com todos os quadros convertidos, para o analisador. */
    private suspend fun buildVideoSheet(frames: List<Bitmap>): VideoSheet {
        val columns = VideoFrameMath.sheetColumns(frames.size)
        val rows = VideoFrameMath.sheetRows(frames.size)
        return VideoSheet(mediaPipeline.createMergedSpriteSheet(frames, columns), columns, rows)
    }

    private data class VideoSheet(val bitmap: Bitmap, val columns: Int, val rows: Int)

    private suspend fun convertOne(bitmap: Bitmap, settings: PixelArtSettings, palette: List<Int>): Bitmap =
        converterPipeline.convert(
            source = bitmap,
            targetWidth = settings.targetResolution,
            targetHeight = settings.targetResolution,
            chromaKeyEnabled = settings.chromaKeyEnabled,
            chromaColorInt = settings.chromaKeyColor.toArgb(),
            chromaTolerance = settings.chromaTolerance,
            contrast = settings.contrast,
            brightness = settings.brightness,
            edgeEnhance = settings.edgeEnhance,
            paletteColorsInt = palette,
            ditherModeName = settings.ditherMode.name,
            chromaModeDistance = settings.chromaMode == io.github.sjos0.spritexp.domain.model.ChromaMode.DISTANCE,
            chromaHueTolerance = settings.chromaHueTolerance,
            chromaSatMin = settings.chromaSatMin,
            chromaValMin = settings.chromaValMin,
            chromaFeather = settings.chromaFeather,
            chromaDespill = settings.chromaDespill,
            extraChromaColorsInt = settings.extraChromaColors.map { it.toArgb() }.toIntArray()
        )

    /** Paleta predefinida, ou extraída da própria imagem no modo Auto. */
    private suspend fun resolvePalette(source: Bitmap?, settings: PixelArtSettings): List<Int> {
        if (settings.palettePreset != PalettePreset.AUTO_EXTRACT) {
            return settings.palettePreset.colors.map { it.toArgb() }
        }
        if (source == null) return listOf(android.graphics.Color.BLACK)
        return withContext(Dispatchers.Default) {
            val res = settings.targetResolution
            // Não reciclamos o bitmap reduzido: se o tamanho já for igual, createScaledBitmap devolve o próprio original.
            val scaled = Bitmap.createScaledBitmap(source, res, res, false)
            val pixels = IntArray(res * res)
            scaled.getPixels(pixels, 0, res, 0, 0, res, res)
            val key = if (settings.chromaKeyEnabled) settings.chromaKeyColor.toArgb() else null
            val colors = PaletteExtractor.extract(pixels, settings.maxAutoColors, key, settings.chromaTolerance)
            colors.ifEmpty { listOf(android.graphics.Color.BLACK) }
        }
    }

    // ---------- Importação ----------

    /** Recebe a imagem decodificada pela tela do conversor. Só aceita 1:1. */
    fun setOriginalImage(bitmap: Bitmap) {
        if (!ImageRules.isSquare(bitmap.width, bitmap.height)) {
            showAlert(ImageRules.SQUARE_ONLY_MESSAGE)
            return
        }
        _uiState.value = _uiState.value.copy(
            originalBitmap = bitmap,
            sourceKind = SourceKind.IMAGE,
            sourceName = "Imagem"
        )
        requestPixelConversion()
    }

    /** Importa uma imagem pelo URI. Valida 1:1 antes de decodificar. */
    fun importImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            val size = withContext(Dispatchers.IO) { mediaPipeline.probeImageSize(uri.toString()) }
            if (size == null || !ImageRules.isSquare(size.first, size.second)) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                showAlert(ImageRules.SQUARE_ONLY_MESSAGE)
                return@launch
            }
            val bmp = withContext(Dispatchers.IO) { mediaPipeline.decodeImage(uri.toString(), maxSide = 512) }
            _uiState.value = _uiState.value.copy(isImporting = false)
            if (bmp == null) {
                showAlert("Não foi possível abrir esta imagem.")
                return@launch
            }
            setOriginalImage(bmp)
        }
    }

    /** Importa um vídeo: lê duração e taxa, usa todos os quadros e extrai o intervalo inteiro. */
    fun importVideo(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            // Vídeos de qualquer proporção são aceitos: cada quadro é recortado em 1:1 pelo centro.
            val timing = withContext(Dispatchers.IO) { mediaPipeline.probeVideoTiming(uri.toString()) }
            if (timing == null) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                showAlert("Não foi possível ler este vídeo.")
                return@launch
            }
            videoUri = uri.toString()
            _uiState.value = _uiState.value.copy(
                sourceKind = SourceKind.VIDEO,
                sourceName = "Vídeo",
                videoTotalFrames = timing.frameCount,
                videoSourceFps = timing.fps,
                videoFps = timing.fps.coerceIn(1, 60),
                videoStartFrame = 1,
                videoEndFrame = minOf(timing.frameCount, MAX_VIDEO_FRAMES),
                currentFrameIndex = 0,
                isPlayingPreview = false
            )
            extractVideoFrames()
            val extracted = rawVideoFrames.size
            showAlert(
                when {
                    extracted == 0 -> "Não foi possível extrair quadros deste vídeo."
                    timing.frameCount > MAX_VIDEO_FRAMES ->
                        "$extracted quadros importados (limite de $MAX_VIDEO_FRAMES). Ajuste o intervalo para escolher outra parte."
                    else -> "$extracted quadros importados."
                }
            )
        }
    }

    /** Importa uma sprite sheet 1:1 para o analisador. */
    fun importSpriteSheet(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            val size = withContext(Dispatchers.IO) { mediaPipeline.probeImageSize(uri.toString()) }
            if (size == null || !ImageRules.isSquare(size.first, size.second)) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                showAlert(ImageRules.SQUARE_ONLY_MESSAGE)
                return@launch
            }
            val sheet = withContext(Dispatchers.IO) { mediaPipeline.decodeImage(uri.toString(), maxSide = 1024) }
            val s = _uiState.value
            _uiState.value = s.copy(isImporting = false)
            if (sheet == null) {
                showAlert("Não foi possível abrir esta sprite sheet.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(
                analyzerSheetBitmap = sheet,
                sourceKind = SourceKind.SHEET,
                sourceName = "Sprite Sheet",
                analyzerSelectedRangeStart = 1,
                analyzerSelectedRangeEnd = s.analyzerColumns * s.analyzerRows
            )
        }
    }

    // ---------- Vídeo e timeline ----------

    /**
     * Intervalo em quadros da origem (1 based), limitado ao máximo por importação.
     * Se os quadros do intervalo já estão em memória, só reconverte (sem reimportar o vídeo).
     * Só extrai de novo quando o intervalo sai do que foi carregado.
     */
    fun setVideoInterval(start: Int, end: Int) {
        val total = _uiState.value.videoTotalFrames
        if (total == 0) return
        val s = start.coerceIn(1, total)
        val e = end.coerceIn(s, minOf(total, s + MAX_VIDEO_FRAMES - 1))
        _uiState.value = _uiState.value.copy(videoStartFrame = s, videoEndFrame = e)
        if (VideoFrameMath.localIndices(rawRangeStart, rawRangeEnd, s, e) != null) {
            videoJob?.cancel() // uma extração pendente não é mais necessária
            scheduleDebouncedConversion()
        } else {
            scheduleVideoExtract()
        }
    }

    /** Quadros da origem dentro do intervalo atual, fatiados do buffer já extraído. */
    private fun activeRawFrames(state: MainUiState): List<Bitmap> {
        val r = VideoFrameMath.localIndices(rawRangeStart, rawRangeEnd, state.videoStartFrame, state.videoEndFrame)
            ?: return emptyList()
        if (r.first >= rawVideoFrames.size) return emptyList()
        return rawVideoFrames.subList(r.first, minOf(r.last + 1, rawVideoFrames.size))
    }

    /** Velocidade da prévia de animação. Não reextrai os quadros. */
    fun setVideoFps(fps: Int) {
        _uiState.value = _uiState.value.copy(videoFps = fps.coerceIn(1, 60))
    }

    private fun scheduleVideoExtract() {
        if (videoUri == null) return
        videoJob?.cancel()
        videoJob = viewModelScope.launch {
            delay(350) // espera o usuário terminar de ajustar
            extractVideoFrames()
        }
    }

    private suspend fun extractVideoFrames() {
        val uri = videoUri ?: return
        val s = _uiState.value
        extractInFlight = true
        _uiState.update { it.copy(isImporting = true, importProgress = 0f) }
        var extracted = false
        try {
            val frames = withContext(Dispatchers.IO) {
                mediaPipeline.extractFrames(
                    videoUriString = uri,
                    firstFrame = s.videoStartFrame - 1,
                    lastFrame = s.videoEndFrame - 1,
                    sourceFps = s.videoSourceFps,
                    maxFrames = MAX_VIDEO_FRAMES,
                    targetSide = 256,
                    onProgress = { p -> _uiState.update { it.copy(importProgress = p) } }
                )
            }
            rawVideoFrames = frames
            rawRangeStart = s.videoStartFrame
            rawRangeEnd = s.videoStartFrame + frames.size - 1
            _uiState.update { it.copy(currentFrameIndex = 0) }
            extracted = true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Log.e(tag, "Falha ao extrair quadros", e)
            showAlert("Não foi possível extrair os quadros deste vídeo. Tente um intervalo menor.")
        } finally {
            // Sempre limpa o progresso, mesmo com erro ou cancelamento: o botão nunca fica preso.
            extractInFlight = false
            _uiState.update { it.copy(isImporting = false, importProgress = 0f) }
        }
        if (extracted) requestPixelConversion()
    }

    fun selectVideoFrame(index: Int) {
        _uiState.value = _uiState.value.copy(currentFrameIndex = index)
    }

    fun toggleVideoPlay() {
        val playing = !_uiState.value.isPlayingPreview
        _uiState.value = _uiState.value.copy(isPlayingPreview = playing)
        if (playing) startVideoPlaybackLoop() else animationJob?.cancel()
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
                delay(1000L / maxOf(1, _uiState.value.videoFps))
            }
        }
    }

    // ---------- Analisador de Sprite Sheet ----------

    fun setAnalyzerRange(start: Int, end: Int) {
        _uiState.value = _uiState.value.copy(analyzerSelectedRangeStart = start, analyzerSelectedRangeEnd = end)
    }

    fun setAnalyzerGrid(cols: Int, rows: Int) {
        _uiState.value = _uiState.value.copy(
            analyzerColumns = maxOf(1, cols),
            analyzerRows = maxOf(1, rows)
        )
    }

    // ---------- Exportação e projetos ----------

    fun setExportFormat(format: ExportFormat) {
        _uiState.value = _uiState.value.copy(exportFormat = format)
    }

    /** Quadros que a exportação deve usar, de acordo com a origem atual. */
    private fun currentExportFrames(state: MainUiState): List<Bitmap> = when (state.sourceKind) {
        SourceKind.SHEET -> sheetFramesInRange(state)
        SourceKind.VIDEO -> state.videoFrames
        SourceKind.IMAGE, SourceKind.DEMO -> listOfNotNull(state.convertedBitmap)
    }

    /** Recorta da sprite sheet apenas os frames do intervalo selecionado. */
    private fun sheetFramesInRange(state: MainUiState): List<Bitmap> {
        val sheet = state.analyzerSheetBitmap ?: return emptyList()
        val cols = state.analyzerColumns
        val rows = state.analyzerRows
        val total = cols * rows
        val fw = sheet.width / cols
        val fh = sheet.height / rows
        if (fw <= 0 || fh <= 0) return emptyList()
        val start = state.analyzerSelectedRangeStart.coerceIn(1, total)
        val end = state.analyzerSelectedRangeEnd.coerceIn(start, total)
        return (start..end).map { idx ->
            val i = idx - 1
            Bitmap.createBitmap(sheet, (i % cols) * fw, (i / cols) * fh, fw, fh)
        }
    }

    fun executeExport() {
        val snapshot = _uiState.value
        val frames = currentExportFrames(snapshot)
        if (frames.isEmpty()) {
            showAlert("Nada para exportar. Importe uma imagem, um vídeo ou uma sprite sheet primeiro.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportProgress = 0.1f) }
            val file = try {
                writeExport(snapshot, frames)
            } catch (e: Exception) {
                Log.e(tag, "Falha na exportação", e)
                null
            }
            // Copia o resultado para Download/SpriteXP: a pasta de cache pode ser apagada pelo sistema.
            val savedName = file?.let { f ->
                withContext(Dispatchers.IO) { SpriteXpStorage.publish(getApplication(), f) }
            }
            _uiState.update {
                it.copy(
                    exportProgress = 1f,
                    isExporting = false,
                    exportedFilePath = file?.absolutePath,
                    alertMessage = when {
                        file == null -> "Falha ao exportar. Tente novamente."
                        savedName == null ->
                            "Exportado, mas não foi possível salvar em ${SpriteXpStorage.DISPLAY_DIR}. Verifique a permissão de armazenamento."
                        else -> "Exportação salva em ${SpriteXpStorage.DISPLAY_DIR}/$savedName"
                    }
                )
            }
            if (file != null) saveProject(snapshot, frames.size, file.absolutePath)
        }
    }

    private suspend fun writeExport(state: MainUiState, frames: List<Bitmap>): File {
        val base = "SpriteXP_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val isSheet = state.sourceKind == SourceKind.SHEET
        val columns = if (isSheet) state.analyzerColumns else ceil(sqrt(frames.size.toDouble())).toInt()
        val fps = if (isSheet) state.analyzerFps else state.videoFps

        return when (state.exportFormat) {
            ExportFormat.SPRITE_SHEET_PNG -> {
                val sheet = mediaPipeline.createMergedSpriteSheet(frames, columns)
                val file = File(getApplication<Application>().cacheDir, "$base.png")
                file.outputStream().use { sheet.compress(Bitmap.CompressFormat.PNG, 100, it) }
                file
            }
            ExportFormat.ZIP_PNGS -> mediaPipeline.exportSpriteBundle(frames, columns, fps, base)
            ExportFormat.JSON_METADATA -> mediaPipeline.exportJsonMetadata(
                projectName = base,
                frameWidth = frames[0].width,
                frameHeight = frames[0].height,
                columns = columns,
                rows = (frames.size + columns - 1) / columns,
                frameCount = frames.size,
                fps = fps
            )
        }
    }

    private suspend fun saveProject(state: MainUiState, frameCount: Int, path: String) {
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")).format(Date())
        projectRepository.addProject(
            SpriteProject(
                id = System.currentTimeMillis().toString(),
                title = "${state.sourceName} · ${frameCount} quadros",
                dateString = now,
                frameCount = frameCount,
                resolution = state.pixelSettings.targetResolution,
                paletteName = state.pixelSettings.palettePreset.displayName,
                iconCategory = "brush",
                localImagePath = path
            )
        )
    }

    fun deleteProject(id: String) {
        viewModelScope.launch { projectRepository.deleteProject(id) }
    }

    // ---------- Atualização automática ----------

    private fun setUpdate(transform: (UpdateUiState) -> UpdateUiState) {
        _updateState.value = transform(_updateState.value)
    }

    /** Recebe o resultado da instalação enviado pelo PackageInstaller. */
    private fun observeInstallEvents() {
        viewModelScope.launch {
            InstallEvents.events.receiveAsFlow().collect { event ->
                when (event) {
                    InstallEvent.Installed -> setUpdate { it.copy(stage = UpdateStage.IDLE) }
                    is InstallEvent.Failed -> setUpdate {
                        it.copy(stage = UpdateStage.ERROR, message = "Não foi possível instalar: ${event.reason}")
                    }
                }
            }
        }
    }

    /** Mostra uma vez a mensagem de sucesso depois que o app reinicia numa versão nova. */
    private fun announceIfJustUpdated() {
        val prefs = getApplication<Application>().getSharedPreferences("spritexp_update", Context.MODE_PRIVATE)
        val last = prefs.getInt("last_build", -1)
        val current = BuildConfig.VERSION_CODE
        if (last != -1 && last != current) showAlert("SpriteXP atualizado com sucesso!")
        prefs.edit().putInt("last_build", current).apply()
    }

    /**
     * Consulta a release mais recente. Se houver versão nova, a atualização começa sozinha:
     * baixa com barra de progresso dentro do app e abre a instalação do Android.
     * Se o manual for true e não houver novidade, avisa o usuário.
     */
    fun checkForUpdates(manual: Boolean = false) {
        val busy = _updateState.value.stage
        if (busy == UpdateStage.CHECKING || busy == UpdateStage.DOWNLOADING || busy == UpdateStage.INSTALLING) return
        viewModelScope.launch {
            setUpdate { it.copy(stage = UpdateStage.CHECKING, message = null) }
            try {
                val info = updater.fetchLatest()
                if (info != null && updater.isNewer(info)) {
                    setUpdate { it.copy(stage = UpdateStage.AVAILABLE, info = info) }
                } else {
                    setUpdate { it.copy(stage = UpdateStage.IDLE, info = null) }
                    if (manual) showAlert("Você já está com a versão mais recente.")
                }
            } catch (e: Exception) {
                Log.e(tag, "Falha ao verificar atualização", e)
                setUpdate { it.copy(stage = UpdateStage.IDLE) }
                if (manual) showAlert("Não foi possível verificar atualizações agora.")
            }
        }
    }

    /** Botão de atualização e reinício automático: sem confirmação extra dentro do app. */
    fun startUpdate() {
        val info = _updateState.value.info ?: return
        runUpdate(info)
    }

    /** Baixa o APK (progresso no diálogo) e entrega à instalação do Android. */
    private fun runUpdate(info: ReleaseInfo) {
        if (!updater.canInstallPackages()) {
            setUpdate { it.copy(stage = UpdateStage.NEED_PERMISSION, info = info) }
            return
        }
        viewModelScope.launch {
            setUpdate { it.copy(stage = UpdateStage.DOWNLOADING, info = info, progress = 0f) }
            try {
                val apk = updater.download(info) { p -> setUpdate { it.copy(progress = p) } }
                setUpdate { it.copy(stage = UpdateStage.INSTALLING) }
                updater.install(apk)
            } catch (e: Exception) {
                Log.e(tag, "Falha no download da atualização", e)
                setUpdate {
                    it.copy(
                        stage = UpdateStage.ERROR,
                        message = "Falha ao baixar a atualização. Verifique a conexão e tente de novo."
                    )
                }
            }
        }
    }

    /**
     * Ao voltar do segundo plano: limpa flags órfãs e refaz só a conversão se faltar prévia.
     * Nunca reimporta o vídeo.
     */
    private fun reconcileAfterBackground() {
        val s = _uiState.value
        if (s.isImporting && !extractInFlight) {
            _uiState.update { it.copy(isImporting = false, importProgress = 0f) }
        }
        if (s.isConverting && conversionJob?.isActive != true) {
            _uiState.update { it.copy(isConverting = false) }
            requestPixelConversion()
        }
    }

    /** Ao voltar do Ajustes com a permissão liberada, continua a atualização sem novo toque. */
    fun onAppResumed() {
        reconcileAfterBackground()
        val state = _updateState.value
        if (state.stage == UpdateStage.NEED_PERMISSION && state.info != null && updater.canInstallPackages()) {
            runUpdate(state.info)
        }
    }

    fun unknownSourcesIntent() = updater.unknownSourcesIntent()

    fun dismissUpdate() {
        setUpdate { it.copy(stage = UpdateStage.IDLE) }
    }

    /** Quadro original atual (posição da animação). Cai para a imagem de demonstração se não houver vídeo. */
    fun currentRawFrame(): Bitmap? {
        val s = _uiState.value
        return activeRawFrames(s).getOrNull(s.currentFrameIndex) ?: s.originalBitmap
    }

    /** Quadro convertido na mesma posição da animação. */
    fun currentConvertedFrame(): Bitmap? {
        val s = _uiState.value
        return s.videoFrames.getOrNull(s.currentFrameIndex) ?: s.convertedBitmap
    }

    private companion object {
        /** Limite de quadros de vídeo por importação, para não estourar a memória. */
        const val MAX_VIDEO_FRAMES = 240 // 8 s a 30 fps: cabe na memória de aparelhos intermediários
    }
}
