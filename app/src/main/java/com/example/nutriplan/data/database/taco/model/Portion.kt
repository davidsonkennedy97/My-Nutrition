package com.example.nutriplan.data.taco.model

/**
 * Enumeração para as unidades de medida suportadas, incluindo unidades métricas e medidas caseiras.
 */
enum class PortionUnit {
    G,          // Gramas
    ML,         // Mililitros
    UNIT,       // Unidade
    COLHER_DE_SOPA,  // Colher de sopa
    COLHER_DE_CHA,   // Colher de chá
    XICARA,     // Xícara
    CONCHA,     // Concha
    FATIA       // Fatia
}

/**
 * Modelo de dados para representar uma porção de alimento, incluindo medidas caseiras e equivalentes em gramas ou mililitros.
 *
 * @param id Identificador único da porção.
 * @param label Rótulo exibível para a porção (ex.: "arroz", "água").
 * @param quantity Quantidade da porção (ex.: 2.0 para 2 colheres).
 * @param unit Unidade da medida (enum PortionUnit).
 * @param gramsEquivalent Equivalente em gramas, se aplicável (pode ser null).
 * @param millilitersEquivalent Equivalente em mililitros, se aplicável (pode ser null).
 */
data class Portion(
    val id: String,
    val label: String,
    val quantity: Float,
    val unit: PortionUnit,
    val gramsEquivalent: Float?,
    val millilitersEquivalent: Float?
) {
    init {
        // Validação para garantir que pelo menos um equivalente (gramas ou mililitros) seja fornecido.
        require(gramsEquivalent != null || millilitersEquivalent != null) {
            "Pelo menos um equivalente (gramas ou mililitros) deve ser fornecido."
        }
    }

    /** Retorna o equivalente em gramas, ou null se não aplicável. */
    fun equivalentGramsOrNull(): Float? = gramsEquivalent

    /** Retorna o equivalente em mililitros, ou null se não aplicável. */
    fun equivalentMillilitersOrNull(): Float? = millilitersEquivalent

    /**
     * Gera um texto exibível para a UI.
     * Ex.: "2.0 colher de sopa de arroz" ou "200.0 ml de água".
     */
    fun displayText(): String {
        val unitStr = when (unit) {
            PortionUnit.G -> "g"
            PortionUnit.ML -> "ml"
            PortionUnit.UNIT -> "unidade"
            PortionUnit.COLHER_DE_SOPA -> "colher de sopa"
            PortionUnit.COLHER_DE_CHA -> "colher de chá"
            PortionUnit.XICARA -> "xícara"
            PortionUnit.CONCHA -> "concha"
            PortionUnit.FATIA -> "fatia"
        }
        return "$quantity $unitStr de $label"
    }
}