package com.example.nutriplan.data.taco.model

enum class FoodBaseUnit {
    PER_100G,
    PER_100ML
}

data class FoodItem(
    val id: String,
    val nome: String,
    val marca: String? = null,
    val origem: String? = null,

    /**
     * Nutrientes de referência do alimento.
     * Ex.: se baseUnit = PER_100G, então nutrientsBase é "por 100g".
     * Ex.: se baseUnit = PER_100ML, então nutrientsBase é "por 100ml".
     */
    val baseUnit: FoodBaseUnit = FoodBaseUnit.PER_100G,
    val nutrientsBase: Nutrients,

    /**
     * Densidade (g/ml) só é necessária quando:
     * - o alimento está em PER_100G
     * - e você quer permitir porções em ML (ex.: óleos, leite etc.)
     *
     * Para água, pode usar 1.0f.
     */
    val densityGPerMl: Float? = null,

    /**
     * Porções/medidas caseiras disponíveis para este alimento.
     * Ex.: "2 colheres de sopa", "1 xícara", "200 ml".
     */
    val portions: List<Portion> = emptyList()
) {
    fun displayName(): String {
        return if (!marca.isNullOrBlank()) "$nome ($marca)" else nome
    }

    /**
     * Calcula nutrientes para uma porção (medida caseira).
     * - Se baseUnit = PER_100G, escala por gramas equivalentes
     * - Se baseUnit = PER_100ML, escala por ml equivalentes
     */
    fun nutrientsFor(portion: Portion): Nutrients {
        val factor = when (baseUnit) {
            FoodBaseUnit.PER_100G -> {
                val grams = portion.gramsEquivalent
                    ?: portion.millilitersEquivalent?.let { ml ->
                        val density = densityGPerMl
                            ?: error("densityGPerMl é obrigatório para converter ml->g em $nome")
                        ml * density
                    }
                    ?: error("Porção sem equivalente em g/ml: ${portion.id}")

                grams / 100f
            }

            FoodBaseUnit.PER_100ML -> {
                val ml = portion.millilitersEquivalent
                    ?: error("Porção precisa ter millilitersEquivalent para alimento PER_100ML: $nome")
                ml / 100f
            }
        }

        return nutrientsBase * factor
    }
}