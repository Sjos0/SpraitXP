package io.github.sjos0.spritexp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sjos0.spritexp.domain.model.SpriteProject
import io.github.sjos0.spritexp.presentation.components.XpButton
import io.github.sjos0.spritexp.presentation.components.XpWindow
import io.github.sjos0.spritexp.ui.theme.PixelifyFontFamily
import io.github.sjos0.spritexp.ui.theme.VT323FontFamily
import io.github.sjos0.spritexp.ui.theme.XpTextBlack
import io.github.sjos0.spritexp.ui.theme.XpTextMuted

/**
 * Meus Projetos screen renderizando ícones Material Symbols sem qualquer recurso estático mock.
 */
@Composable
fun ProjectsScreen(
    projects: List<SpriteProject>,
    onNewProject: () -> Unit,
    onOpenProject: (SpriteProject) -> Unit,
    onDeleteProject: (String) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        XpWindow(
            title = "Meus Projetos",
            icon = {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color(0xFFFFD700)
                )
            },
            onCloseClick = onCloseClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barra Superior com Botão Novo Projeto
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    XpButton(
                        text = "+ Novo",
                        onClick = onNewProject,
                        isPrimaryGreen = true,
                        testTag = "projects_new_button"
                    )
                }

                // Lista de Projetos com Cartão em Relevo Estilo XP
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects, key = { it.id }) { proj ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE4E0D6))
                                .border(1.dp, Color(0xFFACA899), RoundedCornerShape(4.dp))
                                .padding(2.dp)
                                .border(1.dp, Color.White, RoundedCornerShape(3.dp))
                                .clickable { onOpenProject(proj) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ícone Material do Projeto
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0F367D))
                                    .border(1.dp, Color(0xFF245EDC), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val iconVector = when (proj.iconCategory) {
                                    "person" -> Icons.Default.Person
                                    "shield" -> Icons.Default.Shield
                                    "pest_control" -> Icons.Default.BugReport
                                    "brush" -> Icons.Default.Brush
                                    else -> Icons.Default.SportsEsports
                                }
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = proj.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Dados do Projeto
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = proj.title,
                                    fontFamily = PixelifyFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = XpTextBlack
                                )
                                Text(
                                    text = proj.dateString,
                                    fontFamily = VT323FontFamily,
                                    fontSize = 15.sp,
                                    color = XpTextMuted
                                )
                                Text(
                                    text = "${proj.frameCount} frames • ${proj.resolution}px • ${proj.paletteName}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0A246A)
                                )
                            }

                            // Botão de Excluir
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir projeto",
                                tint = Color(0xFFC04018),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { onDeleteProject(proj.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
