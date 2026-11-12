package io.github.sjos0.spritexp.domain.model

/** Preset de ajustes: todo PixelArtSettings + FPS da animação. Intervalo de quadros e export ficam de fora. */
data class SettingsPreset(
    val id: String,
    val name: String,
    val createdAt: Long,
    val settings: PixelArtSettings,
    val videoFps: Int
)
