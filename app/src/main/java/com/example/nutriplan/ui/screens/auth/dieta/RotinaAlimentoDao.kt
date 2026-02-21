package com.example.nutriplan.data.dieta

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface RotinaAlimentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RotinaAlimentoEntity)
}