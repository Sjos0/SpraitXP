package io.github.sjos0.spritexp.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.sjos0.spritexp.data.storage.SpriteXpStorage
import io.github.sjos0.spritexp.domain.model.ChromaMode
import io.github.sjos0.spritexp.domain.model.DitherMode
import io.github.sjos0.spritexp.domain.model.PalettePreset
import io.github.sjos0.spritexp.domain.model.PixelArtSettings
import io.github.sjos0.spritexp.domain.model.SettingsPreset
import org.json.JSONArray
import org.json.JSONObject

/**
 * Presets em Download/SpriteXP/presets.json. Cores são salvas como ARGB inteiro.
 * Arquivo corrompido ou ausente vira lista vazia, sem derrubar o app.
 */
class PresetRepository(private val context: Context) {

    fun load(): List<SettingsPreset> {
        val text = SpriteXpStorage.readText(context, FILE_NAME) ?: return emptyList()
        return runCatching {
            val array = JSONArray(text)
            List(array.length()) { i -> presetFromJson(array.getJSONObject(i)) }
        }.getOrElse { emptyList() }
    }

    fun save(presets: List<SettingsPreset>): Boolean {
        val array = JSONArray()
        presets.forEach { array.put(presetToJson(it)) }
        return SpriteXpStorage.writeText(context, FILE_NAME, array.toString(2))
    }

    private fun presetToJson(p: SettingsPreset): JSONObject = JSONObject().apply {
        put("id", p.id)
        put("name", p.name)
        put("createdAt", p.createdAt)
        put("videoFps", p.videoFps)
        put("settings", settingsToJson(p.settings))
    }

    private fun presetFromJson(o: JSONObject): SettingsPreset = SettingsPreset(
        id = o.getString("id"),
        name = o.getString("name"),
        createdAt = o.optLong("createdAt"),
        videoFps = o.optInt("videoFps", 30),
        settings = settingsFromJson(o.getJSONObject("settings"))
    )

    private fun settingsToJson(s: PixelArtSettings): JSONObject = JSONObject().apply {
        put("targetResolution", s.targetResolution)
        put("displayScale", s.displayScale)
        put("chromaKeyEnabled", s.chromaKeyEnabled)
        put("chromaKeyColor", s.chromaKeyColor.toArgb())
        put("chromaTolerance", s.chromaTolerance.toDouble())
        put("chromaMode", s.chromaMode.name)
        put("chromaHueTolerance", s.chromaHueTolerance.toDouble())
        put("chromaSatMin", s.chromaSatMin.toDouble())
        put("chromaValMin", s.chromaValMin.toDouble())
        put("chromaFeather", s.chromaFeather.toDouble())
        put("chromaDespill", s.chromaDespill)
        put("extraChromaColors", JSONArray().apply { s.extraChromaColors.forEach { put(it.toArgb()) } })
        put("brightness", s.brightness.toDouble())
        put("contrast", s.contrast.toDouble())
        put("edgeEnhance", s.edgeEnhance.toDouble())
        put("palettePreset", s.palettePreset.name)
        put("ditherMode", s.ditherMode.name)
        put("maxAutoColors", s.maxAutoColors)
    }

    private fun settingsFromJson(o: JSONObject): PixelArtSettings {
        val d = PixelArtSettings()
        val extra = o.optJSONArray("extraChromaColors")
        return PixelArtSettings(
            targetResolution = o.optInt("targetResolution", d.targetResolution),
            displayScale = o.optInt("displayScale", d.displayScale),
            chromaKeyEnabled = o.optBoolean("chromaKeyEnabled", d.chromaKeyEnabled),
            chromaKeyColor = Color(o.optInt("chromaKeyColor", d.chromaKeyColor.toArgb())),
            chromaTolerance = o.optDouble("chromaTolerance", d.chromaTolerance.toDouble()).toFloat(),
            chromaMode = enumOr(o.optString("chromaMode"), d.chromaMode),
            chromaHueTolerance = o.optDouble("chromaHueTolerance", d.chromaHueTolerance.toDouble()).toFloat(),
            chromaSatMin = o.optDouble("chromaSatMin", d.chromaSatMin.toDouble()).toFloat(),
            chromaValMin = o.optDouble("chromaValMin", d.chromaValMin.toDouble()).toFloat(),
            chromaFeather = o.optDouble("chromaFeather", d.chromaFeather.toDouble()).toFloat(),
            chromaDespill = o.optBoolean("chromaDespill", d.chromaDespill),
            extraChromaColors = extra?.let { a -> List(a.length()) { i -> Color(a.getInt(i)) } } ?: emptyList(),
            brightness = o.optDouble("brightness", d.brightness.toDouble()).toFloat(),
            contrast = o.optDouble("contrast", d.contrast.toDouble()).toFloat(),
            edgeEnhance = o.optDouble("edgeEnhance", d.edgeEnhance.toDouble()).toFloat(),
            palettePreset = enumOr(o.optString("palettePreset"), d.palettePreset),
            ditherMode = enumOr(o.optString("ditherMode"), d.ditherMode),
            maxAutoColors = o.optInt("maxAutoColors", d.maxAutoColors)
        )
    }

    private inline fun <reified E : Enum<E>> enumOr(name: String, default: E): E =
        runCatching { enumValueOf<E>(name) }.getOrDefault(default)

    companion object {
        const val FILE_NAME = "presets.json"
    }
}
