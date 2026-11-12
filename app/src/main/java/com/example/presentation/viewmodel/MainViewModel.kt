package com.example.presentation.viewmodel

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
import com.example.BuildConfig
import com.example.data.pipeline.MediaProcessingPipeline
import com.example.data.update.AppUpdater
import com.example.data.update.InstallEvent
import com.example.data.update.InstallEvents
import com.example.data.update.UpdateStage
import com.example.data.update.UpdateUiState
import com.example.data.pipeline.PaletteExtractor
import com.example.data.pipeline.PixelArtConverterPipeline
import com.example.data.repository.JsonProjectRepository
import com.example.data.repository.ProjectRepository
import com.example.domain.model.ExportFormat
import com.example.domain.model.PalettePreset
import com.example.domain.model.PixelArtSettings
import com.example.domain.model.SpriteProject
import com.example.domain.usecase.ImageRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

data class MainUiState(
    val currentScreen: String = "splash", // splash, desktop, converter, video, analyzer, palette, export, projects, settings
    val splashProgress: Float = 0.6f,
    val isStartMenuOpen: Boolean = false,
    val isDarkTheme: Boolean = false,
    val soundEnabled: Boolean = true,
    val language: String = "Português",

    // Origem atual
    val sourceKind: SourceKind = SourceKind.DEMO,
    val sourceName: String = "Demo",
    val isImporting: Boolean = false,

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

    private val tag = "SpriteXP"
    private val converterPipeline = PixelArtConverterPipeline()
    private val mediaPipeline = MediaProcessingPipeline(application)
    val projectRepository: ProjectRepository = JsonProjectRepository(application)

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
    private var videoUri: String? = null

    init {
        createDemoBitmaps()
        observeInstallEvents()
        startSplashLoading()
    }

    // ---------- Demonstração inicial ----------

    /** Gera bitmaps de demonstração em memória, sem assets. */
    private fun createDemoBitmaps() {
        try {
            val origBmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(origBmp)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = android.graphics.Color.rgb(0, 255, 0)
            canvas.drawRect(0f, 0f, 64f, 64f, paint)
            paint.color = android.graphics.Color.rgb(36, 94, 220)
            canvas.drawRect(20f, 24f, 44f, 52f, paint)
            paint.color = android.graphics.Color.rgb(255, 204, 170)
            canvas.drawCircle(32f, 18f, 10f, paint)
            paint.color = android.graphics.Color.rgb(10, 36, 106)
            canvas.drawRect(24f, 6f, 40f, 12f, paint)

            val sheetCols = 6
            val sheetRows = 6
            val frameSize = 32
            val sheetBmp = Bitmap.createBitmap(sheetCols * frameSize, sheetRows * frameSize, Bitmap.Config.ARGB_8888)
            val sheetCanvas = Canvas(sheetBmp)
            val demoFrames = mutableListOf<Bitmap>()
            val colors = listOf(
                android.graphics.Color.rgb(36, 94, 220),
                android.graphics.Color.rgb(224, 87, 42),
                android.graphics.Color.rgb(60, 154, 60),
                android.graphics.Color.rgb(126, 37, 83),
                android.graphics.Color.rgb(255, 163, 0),
                android.graphics.Color.rgb(41, 173, 255)
            )
            for (i in 0 until sheetCols * sheetRows) {
                val col = i % sheetCols
                val row = i / sheetCols
                val left = (col * frameSize).toFloat()
                val top = (row * frameSize).toFloat()
                val offset = (i % 6) * 2f
                paint.color = colors[i % colors.size]
                sheetCanvas.drawCircle(left + frameSize / 2f, top + frameSize / 2f - offset + 4f, 8f, paint)
                if (i < 12) {
                    val single = Bitmap.createBitmap(frameSize, frameSize, Bitmap.Config.ARGB_8888)
                    Canvas(single).drawCircle(frameSize / 2f, frameSize / 2f - offset + 4f, 8f, paint)
                    demoFrames.add(single)
                }
            }
            _uiState.value = _uiState.value.copy(
                originalBitmap = origBmp,
                analyzerSheetBitmap = sheetBmp,
                videoFrames = demoFrames
            )
        } catch (e: Exception) {
            Log.e(tag, "Falha ao criar demonstração", e)
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
        val frames = rawVideoFrames
        if (original == null && frames.isEmpty()) return

        val settings = snapshot.pixelSettings
        val version = ++conversionVersion
        conversionJob?.cancel()
        conversionJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConverting = true)

            val palette = resolvePalette(frames.firstOrNull() ?: original, settings)
            val convertedFrames = if (snapshot.sourceKind == SourceKind.VIDEO && frames.isNotEmpty()) {
                frames.map { convertOne(it, settings, palette) }
            } else {
                emptyList()
            }
            val converted = original?.let { convertOne(it, settings, palette) }

            // Ignora resultado de um pedido que já foi substituído.
            if (version == conversionVersion) {
                _uiState.value = _uiState.value.copy(
                    convertedBitmap = converted ?: _uiState.value.convertedBitmap,
                    videoFrames = if (convertedFrames.isNotEmpty()) convertedFrames else _uiState.value.videoFrames,
                    isConverting = false
                )
            }
        }
    }

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
            chromaModeDistance = settings.chromaMode == com.example.domain.model.ChromaMode.DISTANCE,
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

    /** Importa um vídeo. Valida 1:1 e extrai os quadros do intervalo escolhido. */
    fun importVideo(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            // Vídeos de qualquer proporção são aceitos: cada quadro é recortado em 1:1 pelo centro.
            val size = withContext(Dispatchers.IO) { mediaPipeline.probeVideoSize(uri.toString()) }
            if (size == null) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                showAlert("Não foi possível ler este vídeo.")
                return@launch
            }
            videoUri = uri.toString()
            _uiState.value = _uiState.value.copy(
                sourceKind = SourceKind.VIDEO,
                sourceName = "Vídeo",
                videoStartFrame = 1,
                videoEndFrame = 12,
                currentFrameIndex = 0
            )
            extractVideoFrames()
            val count = rawVideoFrames.size
            showAlert(if (count > 0) "$count quadros importados." else "Não foi possível extrair quadros deste vídeo.")
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

    fun setVideoInterval(start: Int, end: Int) {
        val s = maxOf(1, start)
        val e = maxOf(s, end).coerceAtMost(s + MAX_VIDEO_FRAMES - 1)
        _uiState.value = _uiState.value.copy(videoStartFrame = s, videoEndFrame = e)
        scheduleVideoExtract()
    }

    fun setVideoFps(fps: Int) {
        _uiState.value = _uiState.value.copy(videoFps = maxOf(1, minOf(30, fps)))
        scheduleVideoExtract()
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
        val fps = s.videoFps
        val startMs = ((s.videoStartFrame - 1) * 1000L) / fps
        val endMs = (s.videoEndFrame * 1000L) / fps
        val maxFrames = (s.videoEndFrame - s.videoStartFrame + 1).coerceIn(1, MAX_VIDEO_FRAMES)

        _uiState.value = _uiState.value.copy(isImporting = true)
        val frames = withContext(Dispatchers.IO) {
            mediaPipeline.extractFrames(uri, startMs, endMs, fps, maxFrames, targetSide = 256)
        }
        rawVideoFrames = frames
        _uiState.value = _uiState.value.copy(isImporting = false, currentFrameIndex = 0)
        requestPixelConversion()
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
            _uiState.value = _uiState.value.copy(isExporting = true, exportProgress = 0.1f)
            val file = try {
                writeExport(snapshot, frames)
            } catch (e: Exception) {
                Log.e(tag, "Falha na exportação", e)
                null
            }
            _uiState.value = _uiState.value.copy(
                exportProgress = 1f,
                isExporting = false,
                exportedFilePath = file?.absolutePath,
                alertMessage = if (file != null) {
                    "Exportação concluída! Arquivo gerado: ${file.name}"
                } else {
                    "Falha ao exportar. Tente novamente."
                }
            )
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
            ExportFormat.ZIP_PNGS -> mediaPipeline.exportZip(frames, base)
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

    /** Consulta a release mais recente. Se for manual, avisa também quando não há novidade. */
    fun checkForUpdates(manual: Boolean = false) {
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

    /** Baixa o APK com barra de progresso dentro do app e abre a instalação do Android. */
    fun startUpdate() {
        val info = _updateState.value.info ?: return
        viewModelScope.launch {
            setUpdate { it.copy(stage = UpdateStage.DOWNLOADING, progress = 0f) }
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

    fun dismissUpdate() {
        setUpdate { it.copy(stage = UpdateStage.IDLE) }
    }

    private companion object {
        /** Limite de quadros de vídeo por importação, para não estourar a memória. */
        const val MAX_VIDEO_FRAMES = 48
    }
}
