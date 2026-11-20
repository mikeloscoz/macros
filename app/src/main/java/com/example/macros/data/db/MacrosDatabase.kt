package com.example.macros.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

/** Las fechas se guardan como número de día (epochDay): ordenan y comparan bien en SQL. */
class Conversores {
    @TypeConverter
    fun fechaADia(fecha: LocalDate): Long = fecha.toEpochDay()

    @TypeConverter
    fun diaAFecha(dia: Long): LocalDate = LocalDate.ofEpochDay(dia)
}

@Database(
    entities = [Alimento::class, Entrada::class, Peso::class, AjustesDesde::class, DiaExcluido::class],
    version = 3,
    autoMigrations = [
        // 1 → 2: alimentos archivados (columna "archivado" y el nombre deja de ser único a nivel de tabla).
        AutoMigration(from = 1, to = 2),
        // 2 → 3: historial de ajustes y días que no cuentan (tablas nuevas).
        AutoMigration(from = 2, to = 3),
    ],
)
@TypeConverters(Conversores::class)
abstract class MacrosDatabase : RoomDatabase() {
    abstract fun alimentoDao(): AlimentoDao
    abstract fun entradaDao(): EntradaDao
    abstract fun pesoDao(): PesoDao
    abstract fun ajustesDao(): AjustesDao
    abstract fun diaExcluidoDao(): DiaExcluidoDao

    companion object {
        fun crear(context: Context): MacrosDatabase =
            Room.databaseBuilder(context, MacrosDatabase::class.java, "macros.db").build()
    }
}
