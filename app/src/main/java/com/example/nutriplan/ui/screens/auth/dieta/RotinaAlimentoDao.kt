package com.example.nutriplan.data.dieta

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class TotaisNutricionais(
    val totalProteina: Double,
    val totalLipidios: Double,
    val totalCarboidratos: Double,
    val totalCalorias: Double
)

@Dao
interface RotinaAlimentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RotinaAlimentoEntity)

    // Busca todos os itens da rotina com os dados nutricionais do alimento original
    @Query("""
        SELECT
            ra.id                AS itemId,
            ra.rotinaId          AS rotinaId,
            ra.alimentoId        AS alimentoId,
            ra.quantidade        AS quantidade,
            ra.unidade           AS unidade,
            ra.nomeCustom        AS nomeCustom,
            a.alimento           AS nomeOriginal,
            a.quantidadeBase     AS quantidadeBase,
            a.proteina           AS proteinaBase,
            a.lipidios           AS lipidiosBase,
            a.carboidratos       AS carboidratosBase,
            a.calorias           AS caloriasBase
        FROM rotina_alimentos ra
        INNER JOIN alimentos a ON a.id = ra.alimentoId
        WHERE ra.rotinaId = :rotinaId
        ORDER BY ra.createdAt ASC
    """)
    fun getAlimentosDaRotina(rotinaId: Long): Flow<List<RotinaAlimentoComDetalhes>>

    // Busca totais nutricionais de TODAS as rotinas de um paciente
    @Query("""
        SELECT
            COALESCE(SUM((a.proteina     / a.quantidadeBase) * ra.quantidade), 0.0) AS totalProteina,
            COALESCE(SUM((a.lipidios    / a.quantidadeBase) * ra.quantidade), 0.0) AS totalLipidios,
            COALESCE(SUM((a.carboidratos/ a.quantidadeBase) * ra.quantidade), 0.0) AS totalCarboidratos,
            COALESCE(SUM((a.calorias    / a.quantidadeBase) * ra.quantidade), 0.0) AS totalCalorias
        FROM rotina_alimentos ra
        INNER JOIN alimentos a ON a.id = ra.alimentoId
        INNER JOIN rotinas r ON r.id = ra.rotinaId
        WHERE r.pacienteId = :pacienteId
          AND a.quantidadeBase > 0
    """)
    fun getTotaisByPaciente(pacienteId: String): Flow<TotaisNutricionais>

    @Query("UPDATE rotina_alimentos SET quantidade = :quantidade WHERE id = :id")
    suspend fun updateQuantidade(id: Long, quantidade: Double)

    @Query("UPDATE rotina_alimentos SET nomeCustom = :nomeCustom WHERE id = :id")
    suspend fun updateNomeCustom(id: Long, nomeCustom: String?)

    @Query("DELETE FROM rotina_alimentos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
