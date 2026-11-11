package com.example.data.repository

import com.example.domain.model.SpriteProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ProjectRepository {
    fun getProjects(): Flow<List<SpriteProject>>
    suspend fun addProject(project: SpriteProject)
    suspend fun deleteProject(id: String)
}

class InMemoryProjectRepository : ProjectRepository {

    private val _projects = MutableStateFlow<List<SpriteProject>>(
        listOf(
            SpriteProject(
                id = "1",
                title = "Personagem Hero",
                dateString = "Hoje",
                frameCount = 16,
                resolution = 64,
                paletteName = "PICO-8",
                iconCategory = "person"
            ),
            SpriteProject(
                id = "2",
                title = "Guerreiro Armadura",
                dateString = "Ontem",
                frameCount = 12,
                resolution = 48,
                paletteName = "SWEETIE-16",
                iconCategory = "shield"
            ),
            SpriteProject(
                id = "3",
                title = "Inimigo Monstro",
                dateString = "05/11/2026",
                frameCount = 8,
                resolution = 32,
                paletteName = "Game Boy",
                iconCategory = "pest_control"
            ),
            SpriteProject(
                id = "4",
                title = "Item Espada",
                dateString = "28/10/2026",
                frameCount = 4,
                resolution = 32,
                paletteName = "Endesga 32",
                iconCategory = "brush"
            )
        )
    )

    override fun getProjects(): Flow<List<SpriteProject>> = _projects.asStateFlow()

    override suspend fun addProject(project: SpriteProject) {
        val current = _projects.value.toMutableList()
        current.add(0, project)
        _projects.value = current
    }

    override suspend fun deleteProject(id: String) {
        val current = _projects.value.toMutableList()
        current.removeAll { it.id == id }
        _projects.value = current
    }
}
