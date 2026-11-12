package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Endesga32Colors
import com.example.ui.theme.GameBoyColors
import com.example.ui.theme.Pico8Colors
import com.example.ui.theme.Sweetie16Colors

enum class PalettePreset(val displayName: String, val colors: List<Color>) {
    PICO_8("PICO-8 (16)", Pico8Colors),
    SWEETIE_16("SWEETIE-16 (16)", Sweetie16Colors),
    ENDESGA_32("Endesga 32 (32)", Endesga32Colors),
    GAME_BOY("Game Boy (4)", GameBoyColors),
    AUTO_EXTRACT("Auto (Extração)", emptyList());

    companion object {
        fun fromName(name: String): PalettePreset {
            return values().firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: PICO_8
        }
    }
}

enum class DitherMode(val displayName: String) {
    NONE("Nenhum"),
    ORDERED("Ordenado (Bayer 4x4)"),
    FLOYD_STEINBERG("Floyd-Steinberg");

    companion object {
        fun fromName(name: String): DitherMode {
            return values().firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: NONE
        }
    }
}

enum class ExportFormat(val displayName: String, val extension: String) {
    SPRITE_SHEET_PNG("Sprite sheet PNG", "png"),
    ZIP_PNGS("ZIP de PNGs", "zip"),
    JSON_METADATA("JSON de metadados", "json")
}

enum class ChromaMode(val displayName: String) {
    DISTANCE("Distância"),
    HUE("Matiz")
}

data class SpriteFrame(
    val index: Int,
    val isSelected: Boolean = true,
    val durationMs: Long = 100L
)

data class SpriteProject(
    val id: String,
    val title: String,
    val dateString: String,
    val frameCount: Int,
    val resolution: Int,
    val paletteName: String,
    val iconCategory: String = "gamepad",
    val localImagePath: String? = null
)

data class PixelArtSettings(
    val targetResolution: Int = 64,
    val displayScale: Int = 4,
    val chromaKeyEnabled: Boolean = true,
    val chromaKeyColor: Color = Color(0xFF00FF00), // Verde padrão
    val chromaTolerance: Float = 35f, // 0..100
    val chromaMode: ChromaMode = ChromaMode.DISTANCE,
    val chromaHueTolerance: Float = 25f, // 0..90 graus
    val chromaSatMin: Float = 0.35f, // 0..1
    val chromaValMin: Float = 0.12f, // 0..1
    val chromaFeather: Float = 0f, // 0..30
    val chromaDespill: Boolean = false,
    val extraChromaColors: List<Color> = emptyList(), // máx. 3
    val brightness: Float = 0f, // -100..100
    val contrast: Float = 0f, // -100..100
    val edgeEnhance: Float = 0f, // 0..200%
    val palettePreset: PalettePreset = PalettePreset.PICO_8,
    val ditherMode: DitherMode = DitherMode.NONE,
    val maxAutoColors: Int = 16
)
