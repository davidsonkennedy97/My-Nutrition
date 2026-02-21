package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel

@Suppress("UNUSED_PARAMETER")
@Composable
fun DietaTab(
    pacienteId: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    onNavigateToDietaEditor: (String) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToDietaEditor(pacienteId) },
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar dieta"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Plano de Dieta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Área de dieta do paciente (em construção).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}