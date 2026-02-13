package com.example.nutriplan.ui.screens.auth.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutriplan.model.Recipe
import com.example.nutriplan.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    localizedContext: android.content.Context,
    isDarkTheme: Boolean
) {
    var recipes by remember { mutableStateOf(listOf<Recipe>()) }

    // Controla se o editor está aberto
    var isEditing by remember { mutableStateOf(false) }

    // Receita selecionada (quando clica em uma da lista)
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    // Campos do editor
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // Dialog de confirmação para excluir
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Cores para claro/escuro (texto dos TextFields)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val labelColor = if (isDarkTheme) Color.White.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.65f)
    val borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f)
    val placeholderColor = if (isDarkTheme) Color.White.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.45f)

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = textColor,
        unfocusedTextColor = textColor,
        cursorColor = PrimaryGreen,
        focusedLabelColor = PrimaryGreen,
        unfocusedLabelColor = labelColor,
        focusedBorderColor = PrimaryGreen,
        unfocusedBorderColor = borderColor,
        focusedPlaceholderColor = placeholderColor,
        unfocusedPlaceholderColor = placeholderColor
    )

    fun openEditorForNew() {
        selectedRecipe = null
        title = ""
        content = ""
        isEditing = true
    }

    fun openEditorForExisting(recipe: Recipe) {
        selectedRecipe = recipe
        title = recipe.title
        content = recipe.content
        isEditing = true
    }

    fun closeEditor() {
        isEditing = false
        selectedRecipe = null
        title = ""
        content = ""
        showDeleteDialog = false
    }

    fun saveRecipe() {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val current = selectedRecipe
        recipes =
            if (current == null) {
                // Criar nova
                recipes + Recipe(title = trimmedTitle, content = content)
            } else {
                // Atualizar existente (mesmo id)
                recipes.map {
                    if (it.id == current.id) it.copy(title = trimmedTitle, content = content)
                    else it
                }
            }

        closeEditor()
    }

    fun deleteSelectedRecipe() {
        val current = selectedRecipe ?: return
        recipes = recipes.filterNot { it.id == current.id }
        closeEditor()
    }

    Scaffold(
        floatingActionButton = {
            if (!isEditing) {
                FloatingActionButton(
                    onClick = { openEditorForNew() },
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Criar Receita")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isEditing) {
                // Editor (serve para criar OU editar)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título da receita") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = tfColors
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Descrição / Receita") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        placeholder = { Text("Escreva aqui sua receita...") },
                        colors = tfColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ações: Cancelar / Excluir (se estiver editando) / Salvar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { closeEditor() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Cancelar", color = Color.White)
                        }

                        // Só mostra Excluir se for uma receita existente
                        if (selectedRecipe != null) {
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Red
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Excluir")
                            }
                        }

                        Button(
                            onClick = { saveRecipe() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("Salvar", color = Color.White)
                        }
                    }
                }

                // Confirmação de exclusão
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Excluir receita?") },
                        text = { Text("Essa ação não pode ser desfeita.") },
                        confirmButton = {
                            TextButton(onClick = { deleteSelectedRecipe() }) {
                                Text("Excluir", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            } else {
                // Lista
                if (recipes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhuma receita salva.",
                            color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.55f)
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(recipes) { recipe ->
                            Card(
                                onClick = { openEditorForExisting(recipe) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = PrimaryGreen.copy(alpha = 0.10f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = recipe.title,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) Color.White else Color.Black
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