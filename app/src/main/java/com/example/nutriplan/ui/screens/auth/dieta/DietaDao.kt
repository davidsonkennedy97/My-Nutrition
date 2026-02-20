package com.example.nutriplan.data.dieta

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DietaDao {

    // ─── Plano ────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirPlano(plano: DietaPlanoEntity)

    @Delete
    suspend fun deletarPlano(plano: DietaPlanoEntity)

    @Query("SELECT * FROM dieta_plano WHERE pacienteId = :pacienteId ORDER BY dataCriacao DESC")
    fun listarPlanos(pacienteId: String): Flow<List<DietaPlanoEntity>>

    // ─── Refeição ─────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirRefeicao(refeicao: DietaRefeicaoEntity)

    @Delete
    suspend fun deletarRefeicao(refeicao: DietaRefeicaoEntity)

    @Update
    suspend fun atualizarRefeicao(refeicao: DietaRefeicaoEntity)

    @Query("SELECT * FROM dieta_refeicao WHERE planoId = :planoId ORDER BY horario ASC")
    fun listarRefeicoes(planoId: String): Flow<List<DietaRefeicaoEntity>>

    // ─── Item (alimento) ──────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirItem(item: DietaItemEntity)

    @Delete
    suspend fun deletarItem(item: DietaItemEntity)

    @Query("SELECT * FROM dieta_item WHERE refeicaoId = :refeicaoId")
    fun listarItens(refeicaoId: String): Flow<List<DietaItemEntity>>
}
