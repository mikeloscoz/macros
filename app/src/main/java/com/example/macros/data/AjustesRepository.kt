package com.example.macros.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.macros.domain.Meta
import com.example.macros.domain.Sexo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class Ajustes(
    val mantenimiento: Int = 2000,
    val meta: Meta = Meta.Mantener,
    val sexo: Sexo = Sexo.Hombre,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes")

class AjustesRepository(context: Context) {
    private val store = context.dataStore

    private object Claves {
        val mantenimiento = intPreferencesKey("mantenimiento")
        val meta = stringPreferencesKey("meta")
        val sexo = stringPreferencesKey("sexo")
        val alimentosIniciales = booleanPreferencesKey("alimentos_iniciales_cargados")
    }

    val ajustes: Flow<Ajustes> = store.data.map { p ->
        val porDefecto = Ajustes()
        Ajustes(
            mantenimiento = p[Claves.mantenimiento] ?: porDefecto.mantenimiento,
            meta = p[Claves.meta]?.let { n -> Meta.entries.firstOrNull { it.name == n } } ?: porDefecto.meta,
            sexo = p[Claves.sexo]?.let { n -> Sexo.entries.firstOrNull { it.name == n } } ?: porDefecto.sexo,
        )
    }

    suspend fun guardar(ajustes: Ajustes) {
        store.edit {
            it[Claves.mantenimiento] = ajustes.mantenimiento
            it[Claves.meta] = ajustes.meta.name
            it[Claves.sexo] = ajustes.sexo.name
        }
    }

    suspend fun alimentosInicialesCargados(): Boolean =
        store.data.first()[Claves.alimentosIniciales] == true

    suspend fun marcarAlimentosInicialesCargados() {
        store.edit { it[Claves.alimentosIniciales] = true }
    }
}
