package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriplan.data.dieta.AlimentoEntity
import com.example.nutriplan.data.dieta.DietaItemEntity
import com.example.nutriplan.ui.theme.DarkBackground
import com.example.nutriplan.ui.theme.DarkSurface
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscaAlimentoSheet(
    refeicaoId: Int,
    viewModel: DietaViewModel = viewModel(),
    onAdicionarItem: (DietaItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    var alimentoSelecionado by remember { mutableStateOf<AlimentoEntity?>(null) }
    var quantidade by remember { mutableStateOf("100") }

    // Busca reativa via Room — só busca se tiver 2+ caracteres
    val filtrados by produceState(
        initialValue = emptyList<AlimentoEntity>(),
        key1 = busca
    ) {
        if (busca.length < 2) {
            value = emptyList()
        } else {
            viewModel.searchAlimentos(busca).collect { value = it }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Buscar Alimento",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = busca,
                onValueChange = {
                    busca = it
                    alimentoSelecionado = null
                },
                label = { Text("Nome do alimento", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PrimaryGreen
                )
            )

            Spacer(Modifier.height(8.dp))

            if (alimentoSelecionado == null) {
                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                    items(filtrados) { alimento ->
                        Text(
                            text = alimento.alimento, // ← era alimento.nome (AlimentoItem)
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    alimentoSelecionado = alimento
                                    busca = alimento.alimento
                                }
                                .padding(vertical = 8.dp)
                        )
                        HorizontalDivider(color = Color.DarkGray)
                    }
                }
            } else {
                val sel = alimentoSelecionado!!
                val qtd = quantidade.replace(",", ".").toFloatOrNull() ?: 100f
                val fator = qtd / 100f

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            sel.alimento, // ← era sel.nome
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Kcal: %.1f | Prot: %.1fg | Carb: %.1fg | Gord: %.1fg".format(
                                sel.calorias  * fator, // ← era sel.kcal
                                sel.proteina  * fator,
                                sel.carboidratos * fator, // ← era sel.carbo
                                sel.lipidios  * fator  // ← era sel.gordura
                            ),
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade (${sel.unidadeBase})", color = Color.LightGray) }, // ← era sel.unidadePadrao
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PrimaryGreen
                    )
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val q = quantidade.replace(",", ".").toFloatOrNull() ?: 100f
                        val f = q / 100f
                        onAdicionarItem(
                            DietaItemEntity(
                                refeicaoId   = refeicaoId,
                                nomeAlimento = sel.alimento,       // ← era sel.nome
                                quantidade   = q,
                                unidade      = sel.unidadeBase,    // ← era sel.unidadePadrao
                                kcal         = sel.calorias.toFloat()  * f, // ← era sel.kcal
                                proteina     = sel.proteina.toFloat()  * f,
                                carbo        = sel.carboidratos.toFloat() * f, // ← era sel.carbo
                                gordura      = sel.lipidios.toFloat()  * f  // ← era sel.gordura
                            )
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Adicionar", color = Color.White)
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { alimentoSelecionado = null; busca = "" },
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color.Gray)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Buscar outro", color = Color.LightGray)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
