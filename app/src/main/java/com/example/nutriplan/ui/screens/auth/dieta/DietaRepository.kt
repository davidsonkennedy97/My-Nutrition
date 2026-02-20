package com.example.nutriplan.data.dieta

import kotlinx.coroutines.flow.Flow

class DietaRepository(private val dao: DietaDao) {

    // ─── Plano ────────────────────────────────────────────────
    suspend fun inserirPlano(plano: DietaPlanoEntity) =
        dao.inserirPlano(plano)

    suspend fun deletarPlano(plano: DietaPlanoEntity) =
        dao.deletarPlano(plano)

    fun listarPlanos(pacienteId: String): Flow<List<DietaPlanoEntity>> =
        dao.listarPlanos(pacienteId)

    // ─── Refeição ─────────────────────────────────────────────
    suspend fun inserirRefeicao(refeicao: DietaRefeicaoEntity) =
        dao.inserirRefeicao(refeicao)

    suspend fun deletarRefeicao(refeicao: DietaRefeicaoEntity) =
        dao.deletarRefeicao(refeicao)

    suspend fun atualizarRefeicao(refeicao: DietaRefeicaoEntity) =
        dao.atualizarRefeicao(refeicao)

    fun listarRefeicoes(planoId: String): Flow<List<DietaRefeicaoEntity>> =
        dao.listarRefeicoes(planoId)

    // ─── Item (alimento) ──────────────────────────────────────
    suspend fun inserirItem(item: DietaItemEntity) =
        dao.inserirItem(item)

    suspend fun deletarItem(item: DietaItemEntity) =
        dao.deletarItem(item)

    fun listarItens(refeicaoId: String): Flow<List<DietaItemEntity>> =
        dao.listarItens(refeicaoId)
}
