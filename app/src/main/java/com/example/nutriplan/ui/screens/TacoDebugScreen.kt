package com.example.nutriplan.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nutriplan.data.taco.importer.TacoAssetLoader
import com.example.nutriplan.data.taco.importer.TacoRow

@Composable
fun TacoDebugScreen() {
    val appContext = LocalContext.current.applicationContext

    val rowsState = produceState<List<TacoRow>>(initialValue = emptyList(), key1 = appContext) {
        value = TacoAssetLoader.load(
            context = appContext,
            assetFileName = "Taco.csv"
        )
    }

    val rows = rowsState.value

    Column(modifier = Modifier.padding(16.dp)) {
        Text("TACO Debug")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Linhas carregadas: ${rows.size}")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Primeiro item: ${rows.firstOrNull()?.descricao ?: "(vazio)"}")
    }
}