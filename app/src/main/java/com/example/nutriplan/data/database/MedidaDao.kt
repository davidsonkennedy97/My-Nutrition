package com.example.nutriplan.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedidaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)  // ← SOLUÇÃO
    suspend fun inserir(medida: MedidaEntity)

    @Update
    suspend fun atualizar(medida: MedidaEntity)

    @Delete
    suspend fun deletar(medida: MedidaEntity)

    @Query("SELECT * FROM medidas WHERE paciente_id = :pacienteId ORDER BY data_criacao DESC")
    fun buscarPorPaciente(pacienteId: String): Flow<List<MedidaEntity>>

    @Query("SELECT * FROM medidas WHERE paciente_id = :pacienteId ORDER BY data_criacao DESC LIMIT 1")
    suspend fun buscarUltimaMedida(pacienteId: String): MedidaEntity?

    @Query("SELECT * FROM medidas WHERE id = :medidaId")
    suspend fun buscarPorId(medidaId: String): MedidaEntity?

    @Query("DELETE FROM medidas WHERE paciente_id = :pacienteId")
    suspend fun deletarTodasDoPaciente(pacienteId: String)

    @Query("SELECT COUNT(*) FROM medidas WHERE paciente_id = :pacienteId")
    suspend fun contarMedidasDoPaciente(pacienteId: String): Int

    @Query("SELECT altura FROM medidas WHERE paciente_id = :pacienteId ORDER BY data_criacao DESC LIMIT 1")
    suspend fun buscarUltimaAltura(pacienteId: String): Float?
}