package com.example.nutriplan.ui.substitute

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController

@Composable
fun ObserveSubstituteResult(
    navController: NavController,
    onResult: (SubstituteResult) -> Unit
) {
    val handle = navController.currentBackStackEntry?.savedStateHandle ?: return

    val slotKey by handle.getStateFlow(SubstituteNav.RESULT_SLOT_KEY, "").collectAsState()
    val foodId by handle.getStateFlow(SubstituteNav.RESULT_FOOD_ID, "").collectAsState()
    val portionId by handle.getStateFlow(SubstituteNav.RESULT_PORTION_ID, "").collectAsState()

    LaunchedEffect(slotKey, foodId, portionId) {
        if (slotKey.isNotBlank() && foodId.isNotBlank() && portionId.isNotBlank()) {
            onResult(SubstituteResult(slotKey = slotKey, foodId = foodId, portionId = portionId))

            // “limpa” o resultado para não disparar de novo
            handle[SubstituteNav.RESULT_SLOT_KEY] = ""
            handle[SubstituteNav.RESULT_FOOD_ID] = ""
            handle[SubstituteNav.RESULT_PORTION_ID] = ""
        }
    }
}