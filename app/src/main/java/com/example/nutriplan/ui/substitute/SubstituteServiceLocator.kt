package com.example.nutriplan.ui.substitute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nutriplan.data.taco.model.FoodItem

/**
 * DI simples para o módulo de Substituição.
 *
 * - ensureInitialized(...) pode ser chamado várias vezes sem quebrar
 * - Se initialFoods vier preenchido, ele atualiza o catálogo
 * - provideFoodCatalogRepository() expõe o catálogo para o SubstituteNav
 */
object SubstituteServiceLocator {

    private var foodRepo: InMemoryFoodCatalogRepository? = null
    private var allowedRepo: InMemoryAllowedSubstituteRepository? = null

    /**
     * Inicializa repositórios se necessário e (opcionalmente) seta alimentos.
     *
     * Se você chamar com initialFoods não vazio, ele garante que o catálogo
     * seja atualizado mesmo se já estiver inicializado.
     */
    fun ensureInitialized(initialFoods: List<FoodItem> = emptyList()) {
        if (foodRepo == null) {
            // tenta criar já com foods caso venha preenchido
            foodRepo = InMemoryFoodCatalogRepository(initialFoods)
        } else if (initialFoods.isNotEmpty()) {
            // se já existe, mas você passou foods agora, atualiza
            foodRepo?.setFoods(initialFoods)
        }

        if (allowedRepo == null) {
            allowedRepo = InMemoryAllowedSubstituteRepository()
        }
    }

    /**
     * Mantido por compatibilidade com o que você já vinha usando.
     */
    fun init(initialFoods: List<FoodItem>) {
        ensureInitialized(initialFoods)
        setFoods(initialFoods)
    }

    /**
     * Atualiza apenas o catálogo.
     */
    fun setFoods(foods: List<FoodItem>) {
        ensureInitialized()
        foodRepo?.setFoods(foods)
    }

    fun provideFoodCatalogRepository(): FoodCatalogRepository {
        ensureInitialized()
        return foodRepo ?: error("SubstituteServiceLocator não inicializado (foodRepo == null).")
    }

    fun provideOrchestrator(): SubstituteOrchestrator {
        ensureInitialized()
        val fr = foodRepo ?: error("SubstituteServiceLocator não inicializado (foodRepo == null).")
        val ar = allowedRepo ?: error("SubstituteServiceLocator não inicializado (allowedRepo == null).")

        return SubstituteOrchestrator(
            foodCatalogRepository = fr,
            allowedSubstituteRepository = ar
        )
    }

    fun provideViewModelFactory(): ViewModelProvider.Factory {
        ensureInitialized()
        val orchestrator = provideOrchestrator()

        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SubstituteViewModel::class.java)) {
                    return SubstituteViewModel(orchestrator) as T
                }
                throw IllegalArgumentException("ViewModel não suportada: ${modelClass.name}")
            }
        }
    }
}