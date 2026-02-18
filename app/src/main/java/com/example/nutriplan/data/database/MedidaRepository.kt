package com.example.nutriplan.data.repository

import com.example.nutriplan.data.database.MedidaDao
import com.example.nutriplan.data.database.MedidaEntity
import kotlinx.coroutines.flow.Flow

class MedidaRepository(
    private val medidaDao: MedidaDao
) {
    fun medidasDoPaciente(pacienteId: String): Flow<List<MedidaEntity>> =
        medidaDao.buscarPorPaciente(pacienteId)

    suspend fun adicionarNovaMedida(medida: MedidaEntity) {
        // GARANTIA: nova medida precisa ter ID novo
        // Se você estiver passando medida.id de outra medida, isso pode causar overwrite em Update
        // Aqui é INSERT. Com ABORT, se houver colisão (raríssimo), falha ao invés de substituir.
        medidaDao.inserir(medida)
    }

    suspend fun editarMedidaExistente(medida: MedidaEntity) {
        medidaDao.atualizar(medida)
    }

    suspend fun ultimaMedida(pacienteId: String): MedidaEntity? =
        medidaDao.buscarUltimaMedida(pacienteId)
}