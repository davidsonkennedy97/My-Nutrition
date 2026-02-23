package com.example.nutriplan.data.dieta

import androidx.room.ColumnInfo

data class RotinaAlimentoComDetalhes(
    @ColumnInfo(name = "itemId")          val itemId: Long,
    @ColumnInfo(name = "rotinaId")        val rotinaId: Long,
    @ColumnInfo(name = "alimentoId")      val alimentoId: Long,
    @ColumnInfo(name = "quantidade")      val quantidade: Double,
    @ColumnInfo(name = "unidade")         val unidade: String,
    @ColumnInfo(name = "nomeCustom")      val nomeCustom: String?,
    @ColumnInfo(name = "nomeOriginal")    val nomeOriginal: String,
    @ColumnInfo(name = "quantidadeBase")  val quantidadeBase: Double,
    @ColumnInfo(name = "proteinaBase")    val proteinaBase: Double,
    @ColumnInfo(name = "lipidiosBase")    val lipidiosBase: Double,
    @ColumnInfo(name = "carboidratosBase") val carboidratosBase: Double,
    @ColumnInfo(name = "caloriasBase")    val caloriasBase: Double
) {
    // Nome a exibir: custom se tiver, senão o original
    val nomeExibicao: String get() = nomeCustom?.takeIf { it.isNotBlank() } ?: nomeOriginal

    // Recalcula os macros proporcionalmente à quantidade atual
    val proteina: Double    get() = calcular(proteinaBase)
    val lipidios: Double    get() = calcular(lipidiosBase)
    val carboidratos: Double get() = calcular(carboidratosBase)
    val calorias: Double    get() = calcular(caloriasBase)

    private fun calcular(valorBase: Double): Double {
        if (quantidadeBase <= 0) return 0.0
        return (valorBase / quantidadeBase) * quantidade
    }
}