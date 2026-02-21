package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.nutriplan.data.dieta.RotinaEntity
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel

@Composable
fun DietaEditorScreen(
    pacienteId: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    onBack: () -> Unit,
    onThemeToggle: () -> Unit,
    onSave: () -> Unit,
    onOpenRotina: (rotinaId: Long, rotinaNome: String) -> Unit
) {
    val rotinas by dietaViewModel.getRotinas(pacienteId).collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var rotinaEmEdicao by remember { mutableStateOf<RotinaEntity?>(null) }

    Scaffold(
        topBar = {
            Surface(color = PrimaryGreen, contentColor = Color.White) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Plano Alimentar",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onThemeToggle, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Alternar tema",
                                tint = Color.White
                            )
                        }
                        TextButton(onClick = onSave) {
                            Text(
                                text = "Salvar",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        val bodyTextColor = if (isDarkTheme) Color.White else Color.Black

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rotina do Paciente",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = bodyTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    rotinaEmEdicao = null
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Adicionar Rotina")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rotinas, key = { it.id }) { rotina ->
                    RotinaCard(
                        rotina = rotina,
                        isDarkTheme = isDarkTheme,
                        onOpen = { onOpenRotina(rotina.id, rotina.nome) },
                        onEdit = {
                            rotinaEmEdicao = rotina
                            showDialog = true
                        },
                        onDelete = { dietaViewModel.deleteRotina(rotina) }
                    )
                }
            }
        }

        if (showDialog) {
            RotinaDialog(
                isDarkTheme = isDarkTheme,
                rotinaParaEditar = rotinaEmEdicao,
                onDismiss = { showDialog = false },
                onConfirm = { nome, horario ->
                    val editando = rotinaEmEdicao
                    if (editando == null) {
                        dietaViewModel.addRotina(
                            pacienteId = pacienteId,
                            nome = nome,
                            horario = horario
                        )
                    } else {
                        dietaViewModel.updateRotina(editando.copy(nome = nome, horario = horario))
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
private fun RotinaCard(
    rotina: RotinaEntity,
    isDarkTheme: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expand by remember { mutableStateOf(false) }

    val textColor = if (isDarkTheme) Color.White else Color.Black
    val iconColor = if (isDarkTheme) Color.White else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = rotina.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Horário: ${rotina.horario}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.75f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { expand = !expand }) {
                        Icon(
                            imageVector = if (expand) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expandir",
                            tint = iconColor
                        )
                    }

                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = iconColor
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = Color.Red
                        )
                    }
                }
            }

            if (expand) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Alimentos da rotina (em breve).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun RotinaDialog(
    isDarkTheme: Boolean,
    rotinaParaEditar: RotinaEntity?,
    onDismiss: () -> Unit,
    onConfirm: (nome: String, horario: String) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }

    fun formatHora(h: Int, m: Int): String {
        val hh = h.coerceIn(0, 23).toString().padStart(2, '0')
        val mm = m.coerceIn(0, 59).toString().padStart(2, '0')
        return "$hh:$mm"
    }

    LaunchedEffect(rotinaParaEditar?.id) {
        if (rotinaParaEditar != null) {
            nome = rotinaParaEditar.nome
            val parts = rotinaParaEditar.horario.split(":")
            hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
            minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        } else {
            nome = ""
            hour = 7
            minute = 0
        }
    }

    val dialogBg = if (isDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = dialogBg,
            contentColor = textColor,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (rotinaParaEditar == null) "Adicionar Rotina" else "Editar Rotina",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da rotina", color = textColor) },
                    singleLine = true,
                    textStyle = TextStyle(color = textColor),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Horário",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeStepper(
                        label = "Hora",
                        value = hour,
                        min = 0,
                        max = 23,
                        onChange = { hour = it },
                        textColor = textColor
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    TimeStepper(
                        label = "Min",
                        value = minute,
                        min = 0,
                        max = 59,
                        onChange = { minute = it },
                        textColor = textColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sugestões rápidas",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SugestaoButtonVertical("Café da manhã") { nome = "Café da manhã"; hour = 7; minute = 0 }
                    SugestaoButtonVertical("Almoço") { nome = "Almoço"; hour = 12; minute = 0 }
                    SugestaoButtonVertical("Lanche") { nome = "Lanche"; hour = 16; minute = 0 }
                    SugestaoButtonVertical("Janta") { nome = "Janta"; hour = 20; minute = 0 }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val nomeOk = nome.trim()
                        if (nomeOk.isNotEmpty()) {
                            onConfirm(nomeOk, formatHora(hour, minute))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Confirmar", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(1.dp, PrimaryGreen)
                ) {
                    Text("Cancelar", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TimeStepper(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit,
    textColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor.copy(alpha = 0.85f)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    val newValue = if (value <= min) max else (value - 1)
                    onChange(newValue)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            Text(
                text = value.toString().padStart(2, '0'),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.Center,
                color = textColor
            )

            IconButton(
                onClick = {
                    val newValue = if (value >= max) min else (value + 1)
                    onChange(newValue)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun SugestaoButtonVertical(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        border = BorderStroke(1.dp, PrimaryGreen),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = PrimaryGreen,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}