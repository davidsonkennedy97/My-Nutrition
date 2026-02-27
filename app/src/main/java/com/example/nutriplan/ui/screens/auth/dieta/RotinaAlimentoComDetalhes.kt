package com.example.nutriplan.data.dieta

data class RotinaAlimentoComDetalhes(
    val itemId: Long,
    val rotinaId: Long,
    val alimentoId: Long,
    val quantidade: Double,
    val unidade: String,
    val nomeCustom: String?,
    val nomeOriginal: String,
    val quantidadeBase: Double,
    val proteinaBase: Double,
    val lipidiosBase: Double,
    val carboidratosBase: Double,
    val caloriasBase: Double,
    val fibrasBase: Double          // NOVO
) {
    // Fator de conversão: quanto do alimento está sendo usado
    private val fator: Double
        get() = if (quantidadeBase > 0) quantidade / quantidadeBase else 0.0

    // Nome que será exibido na UI
    val nomeExibicao: String
        get() = nomeCustom?.takeIf { it.isNotBlank() } ?: nomeOriginal

    // Valores nutricionais ajustados pela quantidade
    val proteina: Double get() = proteinaBase * fator
    val lipidios: Double get() = lipidiosBase * fator
    val carboidratos: Double get() = carboidratosBase * fator
    val calorias: Double get() = caloriasBase * fator
    val fibras: Double get() = fibrasBase * fator    // NOVO
}
