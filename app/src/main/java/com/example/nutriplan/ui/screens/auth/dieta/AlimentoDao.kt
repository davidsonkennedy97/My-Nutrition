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

    @Query("""
        SELECT * FROM alimentos
        WHERE alimentoNorm LIKE '%' || :qNorm || '%'
        ORDER BY alimento ASC
        LIMIT :limit
    """)
    fun searchNorm(qNorm: String, limit: Int = 5): Flow<List<AlimentoEntity>>
}