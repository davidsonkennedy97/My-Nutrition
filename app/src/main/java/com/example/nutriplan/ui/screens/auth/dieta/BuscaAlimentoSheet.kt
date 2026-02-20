package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutriplan.data.dieta.AlimentoTabela
import com.example.nutriplan.data.dieta.TabelaAlimentosReader
import com.example.nutriplan.ui.theme.PrimaryGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscaAlimentoSheet(
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onAdicionarAlimento: (
        nome: String,
        origem: String,
        quantidade: Float,
        unidade: String,
        proteina: Float,
        lipidios: Float,
        carboidrato: Float,
        calorias: Float
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current

    val bg = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val textSub = if (isDarkTheme) Color.White.copy(0.5f) else Color(0xFF888888)
    val divider = if (isDarkTheme) Color.White.copy(0.08f) else Color(0xFFEEEEEE)

    var query by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf<List<AlimentoTabela>>(emptyList()) }
    var buscando by remember { mutableStateOf(false) }
    var selecionado by remember { mutableStateOf<AlimentoTabela?>(null) }
    var quantidade by remember { mutableStateOf("100") }
    var unidade by remember { mutableStateOf("g") }

    fun buscar() {
        if (query.length < 2) return
        keyboard?.hide()
        buscando = true
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                TabelaAlimentosReader.buscar(context, query)
            }
            resultados = res
            buscando = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = bg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Título ────────────────────────────────────────
            Text(
                "Buscar alimento",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textPrimary
            )

            // ── Barra de busca ────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ex: arroz, frango, banana...") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = PrimaryGreen)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = {
                                query = ""
                                resultados = emptyList()
                                selecionado = null
                            }) {
                                Icon(Icons.Default.Clear, null, tint = textSub)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { buscar() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { buscar() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Buscar", fontWeight = FontWeight.Bold)
                }
            }

            // ── Loading ───────────────────────────────────────
            if (buscando) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }

            // ── Resultados ────────────────────────────────────
            if (!buscando && resultados.isNotEmpty() && selecionado == null) {
                Text(
                    "${resultados.size} resultado(s) encontrado(s)",
                    fontSize = 12.sp,
                    color = textSub
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(resultados) { alimento ->
                        ResultadoItem(
                            alimento = alimento,
                            textPrimary = textPrimary,
                            textSub = textSub,
                            divider = divider,
                            onClick = {
                                selecionado = alimento
                                resultados = emptyList()
                            }
                        )
                    }
                }
            }

            // ── Nenhum resultado ──────────────────────────────
            if (!buscando && resultados.isEmpty() && query.length >= 2 && selecionado == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nenhum alimento encontrado para \"$query\"",
                        color = textSub,
                        fontSize = 13.sp
                    )
                }
            }

            // ── Alimento selecionado ──────────────────────────
            if (selecionado != null) {
                val ali = selecionado!!

                // Card do alimento
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryGreen.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    ali.nome,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Origem: ${ali.origem}  •  ${ali.grupo}",
                                    fontSize = 11.sp,
                                    color = textSub
                                )
                            }
                            IconButton(onClick = { selecionado = null }) {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    tint = textSub
                                )
                            }
                        }

                        HorizontalDivider(color = divider)

                        // Macros por 100g
                        Text(
                            "Valores por 100g:",
                            fontSize = 11.sp,
                            color = textSub,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroChip("Prot.", ali.proteina, Color(0xFFE53935))
                            MacroChip("Lip.", ali.lipidios, Color(0xFFFFA000))
                            MacroChip("Carbo", ali.carboidrato, Color(0xFF1E88E5))
                            MacroChip("Kcal", ali.kcal, PrimaryGreen)
                        }
                    }
                }

                // Quantidade
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = quantidade,
                        onValueChange = { quantidade = it },
                        label = { Text("Quantidade") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = unidade,
                        onValueChange = { unidade = it },
                        label = { Text("Unidade") },
                        modifier = Modifier.width(90.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Macros calculados pela quantidade
                val qtd = quantidade.toFloatOrNull() ?: 100f
                val fator = qtd / 100f
                val protCalc  = ali.proteina    * fator
                val lipCalc   = ali.lipidios    * fator
                val carboCalc = ali.carboidrato * fator
                val kcalCalc  = ali.kcal        * fator

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF252525) else Color(0xFFF5F5F5)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Valores para ${qtd.toInt()}$unidade:",
                            fontSize = 11.sp,
                            color = textSub,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroChip("Prot.", protCalc, Color(0xFFE53935))
                            MacroChip("Lip.", lipCalc, Color(0xFFFFA000))
                            MacroChip("Carbo", carboCalc, Color(0xFF1E88E5))
                            MacroChip("Kcal", kcalCalc, PrimaryGreen)
                        }
                    }
                }

                // Botão adicionar
                Button(
                    onClick = {
                        val q = quantidade.toFloatOrNull() ?: 100f
                        val f = q / 100f
                        onAdicionarAlimento(
                            ali.nome,
                            ali.origem,
                            q,
                            unidade,
                            ali.proteina    * f,
                            ali.lipidios    * f,
                            ali.carboidrato * f,
                            ali.kcal        * f
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Adicionar à refeição",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// ── Item de resultado da busca ─────────────────────────────────
@Composable
private fun ResultadoItem(
    alimento: AlimentoTabela,
    textPrimary: Color,
    textSub: Color,
    divider: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alimento.nome,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                    fontSize = 14.sp
                )
                Text(
                    "${alimento.origem}  •  ${alimento.grupo}",
                    fontSize = 11.sp,
                    color = textSub
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "%.0f kcal".format(alimento.kcal),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Text(
                    "P:%.1f L:%.1f C:%.1f".format(
                        alimento.proteina,
                        alimento.lipidios,
                        alimento.carboidrato
                    ),
                    fontSize = 11.sp,
                    color = textSub
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = divider
        )
    }
}

// ── Chip de macro ──────────────────────────────────────────────
@Composable
private fun MacroChip(label: String, valor: Float, cor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = cor, fontWeight = FontWeight.SemiBold)
        Text("%.1fg".format(valor), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
