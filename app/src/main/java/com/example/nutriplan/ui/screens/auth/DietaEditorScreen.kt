// CAMINHO: ui/screens/auth/DietaEditorScreen.kt
package com.example.nutriplan.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.nutriplan.ui.viewmodel.DietaViewModel

@Composable
fun DietaEditorScreen(
    pacienteId: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    navController: NavController,
    onBack: () -> Unit,
    onSaveAndBackToDietaTab: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Em construção")
    }
}
