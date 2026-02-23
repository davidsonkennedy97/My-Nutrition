package com.example.nutriplan.data.dieta

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RotinaEntity::class,
        AlimentoEntity::class,
        RotinaAlimentoEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class DietaDatabase : RoomDatabase() {

    abstract fun rotinaDao(): RotinaDao
    abstract fun alimentoDao(): AlimentoDao
    abstract fun rotinaAlimentoDao(): RotinaAlimentoDao

    companion object {
        @Volatile private var INSTANCE: DietaDatabase? = null

        fun getInstance(context: Context): DietaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DietaDatabase::class.java,
                    "dieta_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
