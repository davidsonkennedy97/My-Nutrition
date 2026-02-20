package com.example.nutriplan.ui.substitute

import android.content.Context
import android.util.Log
import com.example.nutriplan.data.taco.importer.TacoAssetLoader
import com.example.nutriplan.data.taco.importer.TacoRow
import com.example.nutriplan.data.taco.model.FoodBaseUnit
import com.example.nutriplan.data.taco.model.FoodItem
import com.example.nutriplan.data.taco.model.Nutrients
import com.example.nutriplan.data.taco.model.Portion
import com.example.nutriplan.data.taco.model.PortionUnit
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.Normalizer

object TacoCatalogBootstrapper {

    private const val TAG = "TACO_BOOTSTRAP"

    private val mutex = Mutex()
    private var loaded = false

    /**
     * Lê assets/Taco.csv, converte para FoodItem e injeta no módulo Substituir.
     * Retorna quantos itens foram carregados nesta chamada (0 se já estava carregado).
     */
    suspend fun ensureLoaded(
        context: Context,
        assetFileName: String = "Taco.csv"
    ): Int = mutex.withLock {
        if (loaded) return@withLock 0

        val rows: List<TacoRow> = TacoAssetLoader.load(
            context = context.applicationContext,
            assetFileName = assetFileName
        )

        val foods = mapRowsToFoodItems(rows)

        // ESSENCIAL: alimentar catálogo do módulo Substituir
        SubstituteServiceLocator.setFoods(foods)

        loaded = true

        Log.d(TAG, "Catálogo carregado: ${foods.size} itens (asset=$assetFileName)")
        foods.size
    }

    private fun mapRowsToFoodItems(rows: List<TacoRow>): List<FoodItem> {
        val used = HashMap<String, Int>(rows.size)

        fun uniqueId(base: String): String {
            val key = base.trim().ifBlank { "taco_unknown" }
            val next = (used[key] ?: 0) + 1
            used[key] = next
            return if (next == 1) key else "${key}_$next"
        }

        return rows.mapNotNull { row ->
            val name = row.descricao.trim()
            if (name.isBlank()) return@mapNotNull null

            val baseId = row.numero.trim().ifBlank { slugify(name) }
            val id = uniqueId(baseId)

            val nutrients = Nutrients(
                proteinG = row.proteinaG100g,
                carbsG = row.carboidratoG100g,
                fatG = row.lipideosG100g,
                fiberG = row.fibraG100g,
                sodiumMg = row.sodioMg100g,
                potassiumMg = row.potassioMg100g,
                calciumMg = 0f,
                ironMg = 0f,
                vitaminCMg = 0f
            )

            val defaultPortion = Portion(
                id = "${id}_default_100g",
                label = name,
                quantity = 100f,
                unit = PortionUnit.G,
                gramsEquivalent = 100f,
                millilitersEquivalent = null
            )

            FoodItem(
                id = id,
                nome = name,
                marca = null,
                origem = null,
                baseUnit = FoodBaseUnit.PER_100G,
                nutrientsBase = nutrients,
                densityGPerMl = null,
                portions = listOf(defaultPortion)
            )
        }
    }

    private fun slugify(input: String): String {
        val noAccents = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
        return noAccents
            .lowercase()
            .replace(Regex("""[^a-z0-9]+"""), "_")
            .trim('_')
            .ifBlank { "taco" }
    }
}