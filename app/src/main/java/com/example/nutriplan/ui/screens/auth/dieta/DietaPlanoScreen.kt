package com.example.nutriplan.ui.screens.dieta

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
import com.example.nutriplan.data.dieta.DietaRefeicaoEntity
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel

// Opções padrão de refeição (igual ao WebDiet)
private val refeicoesPadrao = listOf(
    "Café da manhã",
    "Lanche da manhã",
    "Almoço",
    "Lanche da tarde",
    "Jantar",
    "Ceia",
    "Personalizada"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietaPlanoScreen(
    planoId: String,
    tituloPlano: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    onNavigateBack: () -> Unit,
    onAbrirRefeicao: (refeicaoId: String, nomeRefeicao: String) -> Unit
) {
    val bg = if (isDarkTheme) Color(0xFF000000) else Color(0xFFF8F8F8)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val textSub = if (isDarkTheme) Color.White.copy(0.5f) else Color(0xFF888888)

    val refeicoes by dietaViewModel.listarRefeicoes(planoId)
        .collectAsState(initial = emptyList())

    var showNovaRefeicaoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                color = PrimaryGreen
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Text(
                        tituloPlano,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Botão nova refeição ───────────────────────────
            Button(
                onClick = { showNovaRefeicaoDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Nova refeição", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // ── Lista de refeições ────────────────────────────
            if (refeicoes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.NoMeals,
                            null,
                            tint = PrimaryGreen.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "Nenhuma refeição adicionada",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            "Toque em \"Nova refeição\" para começar.",
                            color = textSub,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(refeicoes, key = { it.id }) { refeicao ->
                        RefeicaoCard(
                            refeicao = refeicao,
                            isDarkTheme = isDarkTheme,
                            onClick = { onAbrirRefeicao(refeicao.id, refeicao.nome) },
                            onDelete = { dietaViewModel.deletarRefeicao(refeicao) }
                        )
                    }
                }
            }
        }
    }

    // ── Dialog nova refeição ──────────────────────────────────
    if (showNovaRefeicaoDialog) {
        NovaRefeicaoDialog(
            onDismiss = { showNovaRefeicaoDialog = false },
            onConfirm = { nome, horario ->
                dietaViewModel.criarRefeicao(planoId, nome, horario)
                showNovaRefeicaoDialog = false
            }
        )
    }
}

// ── Card de cada refeição ─────────────────────────────────────
@Composable
private fun RefeicaoCard(
    refeicao: DietaRefeicaoEntity,
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                Text(refeicao.nome, fontWeight = FontWeight.Bold, color = textPrimary)
                Text(refeicao.horario, fontSize = 12.sp, color = textSub)
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = PrimaryGreen
            )
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFCC0000))
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Excluir refeição?") },
            text = { Text("\"${refeicao.nome}\" e todos os alimentos serão removidos.") },
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

// ── Dialog nova refeição ──────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaRefeicaoDialog(
    onDismiss: () -> Unit,
    onConfirm: (nome: String, horario: String) -> Unit
) {
    var selecionada by remember { mutableStateOf(refeicoesPadrao.first()) }
    var personalizada by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova refeição", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Chips de seleção
                Text("Tipo:", fontWeight = FontWeight.SemiBold)
                refeicoesPadrao.forEach { tipo ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selecionada == tipo,
                            onClick = { selecionada = tipo },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                        )
                        Text(tipo)
                    }
                }

                // Campo personalizado
                if (selecionada == "Personalizada") {
                    OutlinedTextField(
                        value = personalizada,
                        onValueChange = { personalizada = it },
                        label = { Text("Nome da refeição") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen
                        )
                    )
                }

                // Horário
                OutlinedTextField(
                    value = horario,
                    onValueChange = { if (it.length <= 5) horario = it },
                    label = { Text("Horário") },
                    placeholder = { Text("08:00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nome = if (selecionada == "Personalizada") personalizada.trim() else selecionada
                    if (nome.isNotBlank()) onConfirm(nome, horario.ifBlank { "00:00" })
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) { Text("Adicionar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
