package com.example.nutriplan.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {

    // Inserir novo paciente
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(paciente: PacienteEntity)

    // Atualizar paciente existente
    @Update
    suspend fun atualizar(paciente: PacienteEntity)

    // Deletar paciente
    @Delete
    suspend fun deletar(paciente: PacienteEntity)

    // Buscar todos os pacientes (OBSERVÁVEL - atualiza automaticamente)
    @Query("SELECT * FROM pacientes ORDER BY dataCriacao DESC")
    fun buscarTodos(): Flow<List<PacienteEntity>>

    // Buscar paciente por ID
    @Query("SELECT * FROM pacientes WHERE id = :pacienteId")
    suspend fun buscarPorId(pacienteId: String): PacienteEntity?

    // Buscar por status
    @Query("SELECT * FROM pacientes WHERE status = :status ORDER BY dataCriacao DESC")
    fun buscarPorStatus(status: String): Flow<List<PacienteEntity>>

    // Buscar por nome (pesquisa)
    @Query("SELECT * FROM pacientes WHERE nome LIKE '%' || :termo || '%' ORDER BY nome ASC")
    fun buscarPorNome(termo: String): Flow<List<PacienteEntity>>

    // Deletar todos os pacientes (útil para testes)
    @Query("DELETE FROM pacientes")
    suspend fun deletarTodos()
}