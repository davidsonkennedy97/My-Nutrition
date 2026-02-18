package com.example.nutriplan.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(paciente: PacienteEntity)

    @Update
    suspend fun atualizar(paciente: PacienteEntity)

    @Delete
    suspend fun deletar(paciente: PacienteEntity)

    @Query("SELECT * FROM pacientes ORDER BY data_criacao DESC")
    fun buscarTodos(): Flow<List<PacienteEntity>>

    @Query("SELECT * FROM pacientes WHERE id = :pacienteId")
    suspend fun buscarPorId(pacienteId: String): PacienteEntity?

    @Query("SELECT * FROM pacientes WHERE status = :status ORDER BY data_criacao DESC")
    fun buscarPorStatus(status: String): Flow<List<PacienteEntity>>

    @Query("SELECT * FROM pacientes WHERE nome LIKE '%' || :termo || '%' ORDER BY nome ASC")
    fun buscarPorNome(termo: String): Flow<List<PacienteEntity>>

    @Query("DELETE FROM pacientes")
    suspend fun deletarTodos()
}