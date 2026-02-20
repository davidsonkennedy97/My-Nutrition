package com.example.nutriplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutriplan.data.dieta.DietaPlanoEntity
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DietaTab(
    pacienteId: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    onNavigateToDietEditor: (String) -> Unit
) {
    val bg = if (isDarkTheme) Color(0xFF000000) else Color(0xFFF8F8F8)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val textSub = if (isDarkTheme) Color.White.copy(0.5f) else Color(0xFF888888)

    val planos by dietaViewModel.listarPlanos(pacienteId)
        .collectAsState(initial = emptyList())

    var showNovoPlanDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ── Botão novo plano ──────────────────────────────────
        Button(
            onClick = { showNovoPlanDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Novo plano alimentar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // ── Lista de planos ───────────────────────────────────
        if (planos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = PrimaryGreen.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "Nenhum plano criado",
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        "Toque em \"Novo plano alimentar\" para começar.",
                        color = textSub,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            Text(
                "${planos.size} plano(s) criado(s)",
                color = textSub,
                fontSize = 13.sp
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(planos, key = { it.id }) { plano ->
                    PlanoCard(
                        plano = plano,
                        isDarkTheme = isDarkTheme,
                        onClick = { onNavigateToDietEditor(plano.id) },
                        onDelete = { dietaViewModel.deletarPlano(plano) }
                    )
                }
            }
        }
    }

    // ── Dialog novo plano ─────────────────────────────────────
    if (showNovoPlanDialog) {
        NovoPlanDialog(
            onDismiss = { showNovoPlanDialog = false },
            onConfirm = { titulo ->
                val hoje = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                dietaViewModel.criarPlano(pacienteId, titulo, hoje)
                showNovoPlanDialog = false
            }
        )
    }
}

// ── Card de cada plano ────────────────────────────────────────
@Composable
private fun PlanoCard(
    plano: DietaPlanoEntity,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val cardBg = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val textSub = if (isDarkTheme) Color.White.copy(0.5f) else Color(0xFF888888)
    var showConfirm by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(PrimaryGreen, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Restaurant, null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(plano.titulo, fontWeight = FontWeight.Bold, color = textPrimary)
                Text("Criado em ${plano.dataCriacao}", fontSize = 12.sp, color = textSub)
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFCC0000))
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Excluir plano?") },
            text = { Text("\"${plano.titulo}\" será removido permanentemente.") },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete() }) {
                    Text("Excluir", color = Color(0xFFCC0000), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

// ── Dialog para criar novo plano ──────────────────────────────
@Composable
private fun NovoPlanDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo plano alimentar", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Nome do plano") },
                placeholder = { Text("Ex: Plano Emagrecimento") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    focusedLabelColor = PrimaryGreen
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (titulo.isNotBlank()) onConfirm(titulo.trim()) },
                enabled = titulo.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) { Text("Criar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
