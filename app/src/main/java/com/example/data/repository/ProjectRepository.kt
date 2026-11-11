package com.example.data.repository

import android.content.Context
import com.example.domain.model.SpriteProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

interface ProjectRepository {
    fun getProjects(): Flow<List<SpriteProject>>
    suspend fun addProject(project: SpriteProject)
    suspend fun deleteProject(id: String)
}

/**
 * Biblioteca de projetos persistida em JSON no armazenamento interno do app.
 * Usa org.json, que já vem no Android, então não adiciona dependência.
 */
class JsonProjectRepository(context: Context) : ProjectRepository {

    private val file = File(context.filesDir, "projects.json")
    private val lock = Mutex()
    private val state = MutableStateFlow(readAll())

    override fun getProjects(): Flow<List<SpriteProject>> = state.asStateFlow()

    override suspend fun addProject(project: SpriteProject) {
        update { list -> list.filterNot { it.id == project.id } + project }
    }

    override suspend fun deleteProject(id: String) {
        update { list -> list.filterNot { it.id == id } }
    }

    private suspend fun update(transform: (List<SpriteProject>) -> List<SpriteProject>) {
        withContext(Dispatchers.IO) {
            lock.withLock {
                val next = transform(state.value)
                writeAll(next)
                state.value = next
            }
        }
    }

    private fun readAll(): List<SpriteProject> {
        return try {
            if (!file.exists()) return emptyList()
            val array = JSONArray(file.readText())
            List(array.length()) { i -> fromJson(array.getJSONObject(i)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeAll(list: List<SpriteProject>) {
        val array = JSONArray()
        list.forEach { array.put(toJson(it)) }
        // Escreve em arquivo temporário e troca depois, para não corromper a biblioteca se o app fechar no meio.
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeText(array.toString())
        if (!tmp.renameTo(file)) {
            file.writeText(array.toString())
            tmp.delete()
        }
    }

    private fun toJson(p: SpriteProject): JSONObject = JSONObject()
        .put("id", p.id)
        .put("title", p.title)
        .put("dateString", p.dateString)
        .put("frameCount", p.frameCount)
        .put("resolution", p.resolution)
        .put("paletteName", p.paletteName)
        .put("iconCategory", p.iconCategory)
        .put("localImagePath", p.localImagePath ?: JSONObject.NULL)

    private fun fromJson(o: JSONObject): SpriteProject = SpriteProject(
        id = o.getString("id"),
        title = o.getString("title"),
        dateString = o.getString("dateString"),
        frameCount = o.getInt("frameCount"),
        resolution = o.getInt("resolution"),
        paletteName = o.getString("paletteName"),
        iconCategory = o.optString("iconCategory", "gamepad"),
        localImagePath = if (o.isNull("localImagePath")) null else o.getString("localImagePath")
    )
}
