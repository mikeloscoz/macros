package com.example.macros.data

import android.content.Context
import com.example.macros.data.db.MacrosDatabase
import com.example.macros.domain.ObjetivosDia
import com.example.macros.domain.leerCsvAlimentos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Crea y comparte las piezas de datos de la app (inyección de dependencias a mano). */
class AppContainer(private val context: Context) {
    private val db = MacrosDatabase.crear(context)

    val ajustes = AjustesRepository(context, db.ajustesDao())
    val alimentos = AlimentosRepository(db.alimentoDao())
    val diario = DiarioRepository(db.entradaDao())
    val pesos = PesoRepository(db.pesoDao())
    val diasExcluidos = DiasExcluidosRepository(db.diaExcluidoDao())
    val copias = CopiaSeguridad(db, ajustes)

    /** Objetivos de un día concreto (con los ajustes y el peso de entonces). */
    fun objetivosEn(fecha: LocalDate): Flow<ObjetivosDia?> = observarObjetivosEn(fecha, ajustes, pesos)

    /** Para leer y escribir los archivos que elige el usuario en el selector del sistema. */
    val contentResolver: android.content.ContentResolver get() = context.contentResolver

    /** La primera vez que se abre la app, carga los alimentos de assets/alimentos.csv. */
    suspend fun cargarAlimentosIniciales() {
        if (ajustes.alimentosInicialesCargados()) return
        val texto = withContext(Dispatchers.IO) {
            context.assets.open("alimentos.csv").bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
        alimentos.insertarSiNoExisten(leerCsvAlimentos(texto))
        ajustes.marcarAlimentosInicialesCargados()
    }
}
