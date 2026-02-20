package com.example.nutriplan.data.dieta

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DietaDao {

    // PLANO
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirPlano(plano: DietaPlanoEntity): Long

    @Query("SELECT * FROM dieta_plano WHERE pacienteId = :pacienteId")
    fun getPlanosPorPaciente(pacienteId: Int): Flow<List<DietaPlanoEntity>>

    @Delete
    suspend fun deletarPlano(plano: DietaPlanoEntity)

    // REFEIÇÃO
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirRefeicao(refeicao: DietaRefeicaoEntity): Long

    @Query("SELECT * FROM dieta_refeicao WHERE planoId = :planoId")
    fun getRefeicoesPorPlano(planoId: Int): Flow<List<DietaRefeicaoEntity>>

    @Delete
    suspend fun deletarRefeicao(refeicao: DietaRefeicaoEntity)

    // ITEM
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirItem(item: DietaItemEntity)

    @Query("SELECT * FROM dieta_item WHERE refeicaoId = :refeicaoId")
    fun getItensPorRefeicao(refeicaoId: Int): Flow<List<DietaItemEntity>>

    @Delete
    suspend fun deletarItem(item: DietaItemEntity)
}
