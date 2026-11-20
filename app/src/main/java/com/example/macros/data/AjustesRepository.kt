package com.example.macros.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.macros.data.db.AjustesDao
import com.example.macros.data.db.AjustesDesde
import com.example.macros.domain.Meta
import com.example.macros.domain.Sexo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

data class Ajustes(
    val mantenimiento: Int = 2000,
    val meta: Meta = Meta.Mantener,
    val sexo: Sexo = Sexo.Hombre,
)

fun AjustesDesde.comoAjustes() = Ajustes(mantenimiento, meta, sexo)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes")

/**
 * Ajustes del usuario con su historial: cada cambio se guarda con la fecha desde la que vale,
 * para que los días pasados conserven los objetivos que tenían.
 * (DataStore queda solo para marcas internas y para migrar los ajustes de versiones anteriores.)
 */
class AjustesRepository(context: Context, private val dao: AjustesDao) {
    private val store = context.dataStore
    private val mutex = Mutex()

    private object Claves {
        // Antiguas (antes del historial). Solo se leen para migrar.
        val mantenimiento = intPreferencesKey("mantenimiento")
        val meta = stringPreferencesKey("meta")
        val sexo = stringPreferencesKey("sexo")
        // Marcas
        val alimentosIniciales = booleanPreferencesKey("alimentos_iniciales_cargados")
        val historialMigrado = booleanPreferencesKey("historial_ajustes_migrado")
    }

    /** Historial completo, de más antiguo a más reciente. */
    val historial: Flow<List<AjustesDesde>> = flow {
        migrarSiHaceFalta()
        emitAll(dao.observarHistorial())
    }

    /** Ajustes actuales (los del último cambio). Null si todavía no se han configurado. */
    val actuales: Flow<Ajustes?> = historial.map { it.lastOrNull()?.comoAjustes() }

    /** Guarda unos ajustes que valen desde [desde] (hoy por defecto). Si no cambian nada, no hace nada. */
    suspend fun guardar(ajustes: Ajustes, desde: LocalDate = LocalDate.now()) {
        val vigentes = dao.historial().lastOrNull { !it.fecha.isAfter(desde) }
        if (vigentes?.comoAjustes() == ajustes) return
        dao.guardar(AjustesDesde(desde, ajustes.mantenimiento, ajustes.meta, ajustes.sexo))
    }

    /** Primeros ajustes (pantalla de bienvenida): valen para todos los días, también los anteriores. */
    suspend fun guardarIniciales(ajustes: Ajustes) {
        dao.guardar(AjustesDesde(INICIO, ajustes.mantenimiento, ajustes.meta, ajustes.sexo))
    }

    /**
     * Versiones anteriores guardaban los ajustes sueltos en DataStore. Si se llegaron a guardar, pasan al
     * historial como vigentes "desde siempre". Si no (instalación nueva, o nunca se tocaron y siguen los
     * valores por defecto), el historial queda vacío y la app pide los datos con la bienvenida.
     */
    private suspend fun migrarSiHaceFalta() = mutex.withLock {
        val p = store.data.first()
        if (p[Claves.historialMigrado] == true) return@withLock
        if (dao.historial().isEmpty()) {
            val habiaAjustes = p[Claves.mantenimiento] != null || p[Claves.meta] != null || p[Claves.sexo] != null
            if (habiaAjustes) {
                val porDefecto = Ajustes()
                guardarIniciales(
                    Ajustes(
                        mantenimiento = p[Claves.mantenimiento] ?: porDefecto.mantenimiento,
                        meta = p[Claves.meta]?.let { n -> Meta.entries.firstOrNull { it.name == n } } ?: porDefecto.meta,
                        sexo = p[Claves.sexo]?.let { n -> Sexo.entries.firstOrNull { it.name == n } } ?: porDefecto.sexo,
                    ),
                )
            }
        }
        marcarHistorialMigrado()
    }

    /** Tras importar una copia, no hay nada que migrar. */
    suspend fun marcarHistorialMigrado() {
        store.edit { it[Claves.historialMigrado] = true }
    }

    suspend fun alimentosInicialesCargados(): Boolean =
        store.data.first()[Claves.alimentosIniciales] == true

    suspend fun marcarAlimentosInicialesCargados() {
        store.edit { it[Claves.alimentosIniciales] = true }
    }

    companion object {
        /** Fecha de los primeros ajustes: anterior a cualquier día apuntado. */
        val INICIO: LocalDate = LocalDate.of(2000, 1, 1)
    }
}
