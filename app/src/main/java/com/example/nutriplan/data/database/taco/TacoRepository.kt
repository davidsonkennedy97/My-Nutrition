package com.example.nutriplan.data.taco

interface TacoRepository {
    suspend fun buscarPorNome(query: String): List<TacoFood>
}