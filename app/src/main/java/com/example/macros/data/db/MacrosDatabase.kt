package com.example.macros.data.db

import android.content.Context
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

@Database(entities = [Alimento::class, Entrada::class, Peso::class], version = 1)
@TypeConverters(Conversores::class)
abstract class MacrosDatabase : RoomDatabase() {
    abstract fun alimentoDao(): AlimentoDao
    abstract fun entradaDao(): EntradaDao
    abstract fun pesoDao(): PesoDao

    companion object {
        fun crear(context: Context): MacrosDatabase =
            Room.databaseBuilder(context, MacrosDatabase::class.java, "macros.db").build()
    }
}
