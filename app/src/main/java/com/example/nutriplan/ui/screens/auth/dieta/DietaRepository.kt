package com.example.nutriplan.data.dieta

import kotlinx.coroutines.flow.Flow

class DietaRepository(private val dao: DietaDao) {

    suspend fun inserirPlano(plano: DietaPlanoEntity): Long = dao.inserirPlano(plano)
    fun getPlanosPorPaciente(pacienteId: Int): Flow<List<DietaPlanoEntity>> = dao.getPlanosPorPaciente(pacienteId)
    suspend fun deletarPlano(plano: DietaPlanoEntity) = dao.deletarPlano(plano)

    suspend fun inserirRefeicao(refeicao: DietaRefeicaoEntity): Long = dao.inserirRefeicao(refeicao)
    fun getRefeicoesPorPlano(planoId: Int): Flow<List<DietaRefeicaoEntity>> = dao.getRefeicoesPorPlano(planoId)
    suspend fun deletarRefeicao(refeicao: DietaRefeicaoEntity) = dao.deletarRefeicao(refeicao)

    suspend fun inserirItem(item: DietaItemEntity) = dao.inserirItem(item)
    fun getItensPorRefeicao(refeicaoId: Int): Flow<List<DietaItemEntity>> = dao.getItensPorRefeicao(refeicaoId)
    suspend fun deletarItem(item: DietaItemEntity) = dao.deletarItem(item)
}
