package com.example.nutriplan.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PacienteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Acesso ao DAO de pacientes
    abstract fun pacienteDao(): PacienteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Se já existe uma instância, retorna ela
            return INSTANCE ?: synchronized(this) {
                // Se não existe, cria uma nova
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutriplan_database" // Nome do banco de dados
                )
                    .fallbackToDestructiveMigration() // Se mudar a versão, recria o banco
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}