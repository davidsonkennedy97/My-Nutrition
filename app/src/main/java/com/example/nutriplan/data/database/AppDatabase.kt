package com.example.nutriplan.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nutriplan.data.dieta.DietaDao
import com.example.nutriplan.data.dieta.DietaItemEntity
import com.example.nutriplan.data.dieta.DietaPlanoEntity
import com.example.nutriplan.data.dieta.DietaRefeicaoEntity

@Database(
    entities = [
        PacienteEntity::class,
        MedidaEntity::class,
        DietaPlanoEntity::class,
        DietaRefeicaoEntity::class,
        DietaItemEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pacienteDao(): PacienteDao
    abstract fun medidaDao(): MedidaDao
    abstract fun dietaDao(): DietaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutriplan_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
