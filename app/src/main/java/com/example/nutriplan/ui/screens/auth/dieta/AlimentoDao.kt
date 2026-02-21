package com.example.nutriplan.data.dieta

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlimentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AlimentoEntity>)

    @Query("SELECT COUNT(*) FROM alimentos")
    suspend fun countAll(): Int

    // 🔹 Busca COM limite (autocomplete/sugestões)
    // ✅ Aceita qNorm com ou sem '%' (ex: "arro" ou "%arro%")
    @Query(
        """
        SELECT * FROM alimentos
        WHERE alimentoNorm LIKE '%' || REPLACE(:qNorm, '%', '') || '%'
        ORDER BY alimento ASC
        LIMIT :limit
        """
    )
    fun searchNorm(qNorm: String, limit: Int = 20): Flow<List<AlimentoEntity>>

    // ✅ Busca SEM limite
    // ✅ Aceita qNorm com ou sem '%' (ex: "arro" ou "%arro%")
    @Query(
        """
        SELECT * FROM alimentos
        WHERE alimentoNorm LIKE '%' || REPLACE(:qNorm, '%', '') || '%'
        ORDER BY alimento ASC
        """
    )
    fun searchNormAll(qNorm: String): Flow<List<AlimentoEntity>>

    // (debug rápido quando “não aparece nada”)
    @Query("SELECT alimento, origem FROM alimentos ORDER BY alimento ASC LIMIT :limit")
    suspend fun debugTop(limit: Int = 10): List<AlimentoNomeOrigem>

    data class AlimentoNomeOrigem(
        val alimento: String,
        val origem: String
    )
}