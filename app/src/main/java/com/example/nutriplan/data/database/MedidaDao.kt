package com.example.nutriplan.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedidaDao {

    // Nova medida = inserir (NUNCA usar update aqui)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(medida: MedidaEntity)

    // Editar medida existente = atualizar
    @Update
    suspend fun atualizar(medida: MedidaEntity)

    @Delete
    suspend fun deletar(medida: MedidaEntity)

    // ORDEM: mais velho primeiro (em cima), mais novo por último (embaixo)
    @Query("SELECT * FROM medidas WHERE paciente_id = :pacienteId ORDER BY data_criacao ASC, id ASC")
    fun buscarPorPaciente(pacienteId: String): Flow<List<MedidaEntity>>

    // Última medida (para prefill, etc.)
    @Query("SELECT * FROM medidas WHERE paciente_id = :pacienteId ORDER BY data_criacao DESC, id DESC LIMIT 1")
    suspend fun buscarUltimaMedida(pacienteId: String): MedidaEntity?

    @Query("SELECT * FROM medidas WHERE id = :medidaId")
    suspend fun buscarPorId(medidaId: String): MedidaEntity?

    @Query("DELETE FROM medidas WHERE paciente_id = :pacienteId")
    suspend fun deletarTodasDoPaciente(pacienteId: String)

    @Query("SELECT COUNT(*) FROM medidas WHERE paciente_id = :pacienteId")
    suspend fun contarMedidasDoPaciente(pacienteId: String): Int

    @Query("SELECT altura FROM medidas WHERE paciente_id = :pacienteId ORDER BY data_criacao DESC, id DESC LIMIT 1")
    suspend fun buscarUltimaAltura(pacienteId: String): Float?
}