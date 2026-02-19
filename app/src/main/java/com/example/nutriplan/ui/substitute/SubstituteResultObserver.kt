package com.example.nutriplan.ui.substitute

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.NavController

data class SubstituteResult(
    val slotKey: String,
    val foodId: String,
    val portionId: String
)

object SubstituteResultObserver {

    /**
     * Observa o resultado de Substituição na backstack anterior.
     *
     * Uso típico:
     * - Chame isso na tela da refeição (onde você abriu o Substituir).
     * - Quando voltar, ele dispara onResult e limpa as chaves.
     */
    fun observe(
        navController: NavController,
        lifecycleOwner: LifecycleOwner,
        onResult: (SubstituteResult) -> Unit
    ) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return

        val slotKeyLive = handle.getLiveData<String>(SubstituteNav.RESULT_SLOT_KEY)
        val foodIdLive = handle.getLiveData<String>(SubstituteNav.RESULT_FOOD_ID)
        val portionIdLive = handle.getLiveData<String>(SubstituteNav.RESULT_PORTION_ID)

        val observer = Observer<Any> {
            val slotKey = handle.get<String>(SubstituteNav.RESULT_SLOT_KEY)
            val foodId = handle.get<String>(SubstituteNav.RESULT_FOOD_ID)
            val portionId = handle.get<String>(SubstituteNav.RESULT_PORTION_ID)

            if (!slotKey.isNullOrBlank() && !foodId.isNullOrBlank() && !portionId.isNullOrBlank()) {
                // dispara callback
                onResult(SubstituteResult(slotKey, foodId, portionId))

                // limpa para não reaplicar
                handle.remove<String>(SubstituteNav.RESULT_SLOT_KEY)
                handle.remove<String>(SubstituteNav.RESULT_FOOD_ID)
                handle.remove<String>(SubstituteNav.RESULT_PORTION_ID)
            }
        }

        // Observa as 3 chaves; qualquer mudança tenta montar o resultado completo
        slotKeyLive.observe(lifecycleOwner, observer)
        foodIdLive.observe(lifecycleOwner, observer)
        portionIdLive.observe(lifecycleOwner, observer)
    }
}