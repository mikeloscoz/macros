package com.example.macros.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AlimentoDao {
    /** Alimentos visibles (no archivados). */
    @Query("SELECT * FROM alimentos WHERE archivado = 0")
    fun observarVisibles(): Flow<List<Alimento>>

    /** Todos, archivados incluidos (para la copia de seguridad). */
    @Query("SELECT * FROM alimentos")
    suspend fun todos(): List<Alimento>

    @Query("SELECT * FROM alimentos WHERE archivado = 0 AND nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun visiblePorNombre(nombre: String): Alimento?

    @Query("SELECT nombre FROM alimentos WHERE archivado = 0")
    suspend fun nombresVisibles(): List<String>

    @Insert
    suspend fun insertar(alimento: Alimento): Long

    @Insert
    suspend fun insertarTodos(alimentos: List<Alimento>)

    @Update
    suspend fun actualizar(alimento: Alimento)

    @Delete
    suspend fun borrar(alimento: Alimento)

    @Query("SELECT COUNT(*) FROM entradas WHERE alimentoId = :alimentoId")
    suspend fun vecesUsado(alimentoId: Long): Int

    @Query("DELETE FROM alimentos")
    suspend fun borrarTodos()
}

@Dao
interface EntradaDao {
    @Transaction
    @Query("SELECT * FROM entradas WHERE fecha = :fecha ORDER BY comida, id")
    fun observarDia(fecha: LocalDate): Flow<List<EntradaConAlimento>>

    @Transaction
    @Query("SELECT * FROM entradas WHERE fecha BETWEEN :desde AND :hasta ORDER BY fecha, comida, id")
    fun observarRango(desde: LocalDate, hasta: LocalDate): Flow<List<EntradaConAlimento>>

    @Query("SELECT * FROM entradas")
    suspend fun todas(): List<Entrada>

    @Insert
    suspend fun insertar(entrada: Entrada): Long

    @Insert
    suspend fun insertarTodas(entradas: List<Entrada>)

    @Update
    suspend fun actualizar(entrada: Entrada)

    @Delete
    suspend fun borrar(entrada: Entrada)

    @Query("DELETE FROM entradas")
    suspend fun borrarTodas()
}

@Dao
interface PesoDao {
    @Query("SELECT * FROM pesos ORDER BY fecha DESC LIMIT 1")
    fun observarUltimo(): Flow<Peso?>

    @Query("SELECT * FROM pesos ORDER BY fecha")
    fun observarTodos(): Flow<List<Peso>>

    @Query("SELECT * FROM pesos")
    suspend fun todos(): List<Peso>

    /** Si ya hay peso ese día, lo sobrescribe. */
    @Upsert
    suspend fun guardar(peso: Peso)

    @Upsert
    suspend fun guardarTodos(pesos: List<Peso>)

    @Query("DELETE FROM pesos")
    suspend fun borrarTodos()
}
