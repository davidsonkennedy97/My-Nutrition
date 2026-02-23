package com.example.nutriplan.data.dieta

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RotinaAlimentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RotinaAlimentoEntity)

    // Busca todos os itens da rotina com os dados nutricionais do alimento original
    @Query("""
        SELECT 
            ra.id           AS itemId,
            ra.rotinaId     AS rotinaId,
            ra.alimentoId   AS alimentoId,
            ra.quantidade   AS quantidade,
            ra.unidade      AS unidade,
            ra.nomeCustom   AS nomeCustom,
            a.alimento      AS nomeOriginal,
            a.quantidadeBase AS quantidadeBase,
            a.proteina      AS proteinaBase,
            a.lipidios      AS lipidiosBase,
            a.carboidratos  AS carboidratosBase,
            a.calorias      AS caloriasBase
        FROM rotina_alimentos ra
        INNER JOIN alimentos a ON a.id = ra.alimentoId
        WHERE ra.rotinaId = :rotinaId
        ORDER BY ra.createdAt ASC
    """)
    fun getAlimentosDaRotina(rotinaId: Long): Flow<List<RotinaAlimentoComDetalhes>>

    @Query("UPDATE rotina_alimentos SET quantidade = :quantidade WHERE id = :id")
    suspend fun updateQuantidade(id: Long, quantidade: Double)

    @Query("UPDATE rotina_alimentos SET nomeCustom = :nomeCustom WHERE id = :id")
    suspend fun updateNomeCustom(id: Long, nomeCustom: String?)

    @Query("DELETE FROM rotina_alimentos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
