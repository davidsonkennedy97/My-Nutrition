package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.nutriplan.data.dieta.AlimentoEntity
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel
import kotlin.math.abs
import java.util.Locale

@Composable
fun RotinaAlimentosScreen(
    rotinaId: Long,
    rotinaNome: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val titulo = rotinaNome.trim().ifEmpty { "Rotina" }
    val textColor = if (isDarkTheme) Color.White else Color.Black

    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // importa todos CSV em assets/tabela/
        dietaViewModel.importAllFoodTablesFromAssets()
    }

    val resultados by dietaViewModel
        .searchAlimentos(query)
        .collectAsState(initial = emptyList())

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
                        text = titulo,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 56.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pesquisar alimento", color = textColor) },
                singleLine = true,
                textStyle = TextStyle(color = textColor)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Só mostra resultados quando digitar algo (como você pediu)
            if (query.trim().isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(resultados, key = { it.id }) { item ->
                        AlimentoResultadoCard(
                            alimento = item,
                            isDarkTheme = isDarkTheme,
                            onClick = {
                                // Clique adiciona direto e silencioso
                                dietaViewModel.addAlimentoNaRotina(rotinaId, item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlimentoResultadoCard(
    alimento: AlimentoEntity,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val subColor = textColor.copy(alpha = 0.75f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = alimento.alimento,
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Origem: ${alimento.origem}  •  Quantidade: ${formatQtd(alimento.quantidadeBase)} ${alimento.unidadeBase}",
                color = subColor,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Proteína: ${format1(alimento.proteina)}  •  Lipídios: ${format1(alimento.lipidios)}  •  Carboidratos: ${format1(alimento.carboidratos)}",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Calorias: ${format1(alimento.calorias)}",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun format1(v: Double): String {
    return String.format(Locale.US, "%.1f", v).replace(".", ",")
}

private fun formatQtd(v: Double): String {
    return if (abs(v - v.toInt()) <= 0.00001) v.toInt().toString() else format1(v)
}