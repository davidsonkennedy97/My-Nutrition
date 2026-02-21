package com.example.nutriplan.data.dieta

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RotinaDao {

    @Query("SELECT * FROM rotinas WHERE pacienteId = :pacienteId ORDER BY horario ASC, createdAt ASC")
    fun getRotinasByPaciente(pacienteId: String): Flow<List<RotinaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rotina: RotinaEntity): Long

    @Update
    suspend fun update(rotina: RotinaEntity)

    @Delete
    suspend fun delete(rotina: RotinaEntity)
}