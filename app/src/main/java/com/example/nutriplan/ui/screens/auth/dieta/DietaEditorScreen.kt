package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.nutriplan.data.dieta.RotinaAlimentoComDetalhes
import com.example.nutriplan.data.dieta.RotinaEntity
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel
import java.util.Locale

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

    var showDialog     by remember { mutableStateOf(false) }
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
                        modifier = Modifier.align(Alignment.CenterStart).size(48.dp)
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
                            Text("Salvar", color = Color.White, fontWeight = FontWeight.Bold)
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
                onClick = { rotinaEmEdicao = null; showDialog = true },
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
                        dietaViewModel = dietaViewModel,
                        onOpen = { onOpenRotina(rotina.id, rotina.nome) },
                        onEdit = { rotinaEmEdicao = rotina; showDialog = true },
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
                        dietaViewModel.addRotina(pacienteId = pacienteId, nome = nome, horario = horario)
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
    dietaViewModel: DietaViewModel,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expand by remember { mutableStateOf(false) }
    val textColor = if (isDarkTheme) Color.White else Color.Black
    var observacaoText by remember(rotina.id) { mutableStateOf(rotina.observacao) }

    val alimentos by dietaViewModel
        .getAlimentosDaRotina(rotina.id)
        .collectAsState(initial = emptyList())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpen() }
                ) {
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
                    if (alimentos.isNotEmpty()) {
                        Text(
                            text = "${alimentos.size} alimento(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryGreen
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (alimentos.isNotEmpty()) {
                        IconButton(onClick = { expand = !expand }) {
                            Icon(
                                imageVector = if (expand) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expandir",
                                tint = textColor
                            )
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = textColor)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Red)
                    }
                }
            }

            if (expand && alimentos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                alimentos.forEach { item ->
                    AlimentoItemCard(
                        item = item,
                        isDarkTheme = isDarkTheme,
                        onQuantidadeChange = { dietaViewModel.updateQuantidadeItem(item.itemId, it) },
                        onNomeChange = { dietaViewModel.updateNomeCustomItem(item.itemId, it) },
                        onDelete = { dietaViewModel.deleteItemRotina(item.itemId) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ── Observação ──
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PrimaryGreen.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Observação",
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = observacaoText,
                onValueChange = { novo ->
                    observacaoText = novo
                    dietaViewModel.updateObservacaoRotina(rotina.id, novo)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Ex: evitar açúcar, preferir integral...",
                        color = textColor.copy(alpha = 0.4f)
                    )
                },
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                ),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = PrimaryGreen.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun AlimentoItemCard(
    item: RotinaAlimentoComDetalhes,
    isDarkTheme: Boolean,
    onQuantidadeChange: (Double) -> Unit,
    onNomeChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val subColor  = textColor.copy(alpha = 0.6f)

    var editandoNome by remember(item.itemId) { mutableStateOf(false) }
    var nomeTemp     by remember(item.itemId) { mutableStateOf(item.nomeExibicao) }
    var qtdText      by remember(item.itemId) { mutableStateOf(formatQtdItem(item.quantidade)) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editandoNome) {
                    OutlinedTextField(
                        value = nomeTemp,
                        onValueChange = { novo: String -> nomeTemp = novo },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = textColor,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                        ),
                        label = { Text("Nome", color = subColor) }
                    )
                    TextButton(onClick = {
                        onNomeChange(nomeTemp.trim())
                        editandoNome = false
                    }) {
                        Text("OK", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = item.nomeExibicao,
                        modifier = Modifier.weight(1f),
                        color = textColor,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                    IconButton(
                        onClick = { nomeTemp = item.nomeExibicao; editandoNome = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar nome",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remover",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Qtd:", color = subColor, style = MaterialTheme.typography.labelSmall)
                BasicTextField(
                    value = qtdText,
                    onValueChange = { novo: String ->
                        qtdText = novo
                        val parsed = novo.replace(",", ".").toDoubleOrNull()
                        if (parsed != null && parsed > 0) onQuantidadeChange(parsed)
                    },
                    modifier = Modifier
                        .width(60.dp)
                        .height(28.dp)
                        .border(1.dp, PrimaryGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = textColor,
                        fontSize = MaterialTheme.typography.labelMedium.fontSize,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            innerTextField()
                        }
                    }
                )
                Text(item.unidade, color = subColor, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacroBolinha(cor = Color(0xFF2196F3), label = "P", valor = item.proteina)
                MacroBolinha(cor = Color(0xFFFF9800), label = "L", valor = item.lipidios)
                MacroBolinha(cor = Color(0xFFF44336), label = "C", valor = item.carboidratos)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${fmt(item.calorias)} kcal",
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MacroBolinha(cor: Color, label: String, valor: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = cor)
        }
        Text(
            text = "$label: ${fmt(valor)}g",
            color = cor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RotinaDialog(
    isDarkTheme: Boolean,
    rotinaParaEditar: RotinaEntity?,
    onDismiss: () -> Unit,
    onConfirm: (nome: String, horario: String) -> Unit
) {
    var nome   by remember { mutableStateOf("") }
    var hour   by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }

    fun formatHora(h: Int, m: Int) =
        "${h.coerceIn(0,23).toString().padStart(2,'0')}:${m.coerceIn(0,59).toString().padStart(2,'0')}"

    LaunchedEffect(rotinaParaEditar?.id) {
        if (rotinaParaEditar != null) {
            nome   = rotinaParaEditar.nome
            val parts = rotinaParaEditar.horario.split(":")
            hour   = parts.getOrNull(0)?.toIntOrNull() ?: 7
            minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        } else { nome = ""; hour = 7; minute = 0 }
    }

    val dialogBg  = if (isDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = dialogBg, contentColor = textColor,
            shape = RoundedCornerShape(16.dp), tonalElevation = 2.dp,
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (rotinaParaEditar == null) "Adicionar Rotina" else "Editar Rotina",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = textColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nome,
                    onValueChange = { novo: String -> nome = novo },
                    label = { Text("Nome da rotina", color = textColor) },
                    singleLine = true,
                    textStyle = TextStyle(color = textColor),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Horário", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeStepper("Hora",  hour,   0, 23, { hour   = it }, textColor)
                    Text(":", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = textColor,
                        modifier = Modifier.padding(horizontal = 10.dp))
                    TimeStepper("Min", minute, 0, 59, { minute = it }, textColor)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Sugestões rápidas", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SugestaoButtonVertical("Café da manhã") { nome = "Café da manhã"; hour = 7;  minute = 0 }
                    SugestaoButtonVertical("Almoço")        { nome = "Almoço";        hour = 12; minute = 0 }
                    SugestaoButtonVertical("Lanche")        { nome = "Lanche";        hour = 16; minute = 0 }
                    SugestaoButtonVertical("Janta")         { nome = "Janta";         hour = 20; minute = 0 }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { val n = nome.trim(); if (n.isNotEmpty()) onConfirm(n, formatHora(hour, minute)) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White)
                ) { Text("Confirmar", fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    border = BorderStroke(1.dp, PrimaryGreen)
                ) { Text("Cancelar", color = PrimaryGreen, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun TimeStepper(
    label: String, value: Int, min: Int, max: Int,
    onChange: (Int) -> Unit, textColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.85f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange(if (value <= min) max else value - 1) },
                modifier = Modifier.size(36.dp)) {
                Text("−", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = textColor)
            }
            Text(value.toString().padStart(2,'0'),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.Center,
                color = textColor)
            IconButton(onClick = { onChange(if (value >= max) min else value + 1) },
                modifier = Modifier.size(36.dp)) {
                Text("+", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}

@Composable
private fun SugestaoButtonVertical(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(46.dp),
        border = BorderStroke(1.dp, PrimaryGreen),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = PrimaryGreen, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

private fun fmt(v: Double): String =
    String.format(Locale.US, "%.1f", v).replace(".", ",")

private fun formatQtdItem(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString()
    else String.format(Locale.US, "%.1f", v).replace(".", ",")
