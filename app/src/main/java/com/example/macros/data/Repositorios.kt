package com.example.macros.data

import com.example.macros.data.db.Alimento
import com.example.macros.data.db.AlimentoDao
import com.example.macros.data.db.Entrada
import com.example.macros.data.db.EntradaConAlimento
import com.example.macros.data.db.EntradaDao
import com.example.macros.data.db.Peso
import com.example.macros.data.db.PesoDao
import com.example.macros.data.db.DiaExcluido
import com.example.macros.data.db.DiaExcluidoDao
import com.example.macros.domain.ObjetivosDia
import com.example.macros.domain.objetivosEn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.Collator
import java.time.LocalDate
import java.util.Locale

class AlimentosRepository(private val dao: AlimentoDao) {
    /** Orden alfabético en castellano (Á junto a A, ñ después de n, sin distinguir mayúsculas). */
    private val collator = Collator.getInstance(Locale.forLanguageTag("es-ES")).apply {
        strength = Collator.SECONDARY
    }

    /** Alimentos visibles (sin los archivados), en orden alfabético. */
    val alimentos: Flow<List<Alimento>> =
        dao.observarVisibles().map { lista -> lista.sortedWith { a, b -> collator.compare(a.nombre, b.nombre) } }

    /** ¿Hay otro alimento visible (distinto de [idExcluido]) con ese nombre? */
    suspend fun nombreOcupado(nombre: String, idExcluido: Long = 0): Boolean {
        val existente = dao.visiblePorNombre(nombre.trim()) ?: return false
        return existente.id != idExcluido
    }

    /** Crea el alimento si su id es 0; si no, lo actualiza. */
    suspend fun guardar(alimento: Alimento) {
        val limpio = alimento.copy(nombre = alimento.nombre.trim())
        if (limpio.id == 0L) dao.insertar(limpio) else dao.actualizar(limpio)
    }

    /**
     * "Borra" el alimento: si está apuntado en el diario, solo lo archiva (desaparece de la lista
     * pero los días pasados no cambian); si no se ha usado nunca, lo borra del todo.
     */
    suspend fun borrar(alimento: Alimento) {
        if (dao.vecesUsado(alimento.id) > 0) dao.actualizar(alimento.copy(archivado = true))
        else dao.borrar(alimento)
    }

    /** Inserta los alimentos cuyo nombre no exista ya entre los visibles. Devuelve cuántos ha añadido. */
    suspend fun insertarSiNoExisten(alimentos: List<Alimento>): Int {
        val existentes = dao.nombresVisibles().map { it.lowercase() }.toMutableSet()
        val nuevos = alimentos.filter { existentes.add(it.nombre.trim().lowercase()) }
        dao.insertarTodos(nuevos)
        return nuevos.size
    }
}

class DiarioRepository(private val dao: EntradaDao) {
    fun dia(fecha: LocalDate): Flow<List<EntradaConAlimento>> = dao.observarDia(fecha)

    fun rango(desde: LocalDate, hasta: LocalDate): Flow<List<EntradaConAlimento>> =
        dao.observarRango(desde, hasta)

    suspend fun anadir(fecha: LocalDate, comida: Int, alimentoId: Long, gramos: Double) {
        require(comida in 1..4)
        dao.insertar(Entrada(fecha = fecha, comida = comida, alimentoId = alimentoId, gramos = gramos))
    }

    suspend fun cambiarGramos(entrada: Entrada, gramos: Double) = dao.actualizar(entrada.copy(gramos = gramos))

    suspend fun borrar(entrada: Entrada) = dao.borrar(entrada)
}

class PesoRepository(private val dao: PesoDao) {
    /** Peso actual = último peso registrado (null si no hay ninguno). */
    val actual: Flow<Peso?> = dao.observarUltimo()

    val historial: Flow<List<Peso>> = dao.observarTodos()

    /** Registra el peso de un día; si ya había uno ese día, lo sustituye. */
    suspend fun registrar(kg: Double, fecha: LocalDate = LocalDate.now()) = dao.guardar(Peso(fecha, kg))

    suspend fun borrar(peso: Peso) = dao.borrar(peso)
}

/** Días marcados para no contar en Progreso. */
class DiasExcluidosRepository(private val dao: DiaExcluidoDao) {
    val excluidos: Flow<Set<LocalDate>> = dao.observarTodos().map { lista -> lista.map { it.fecha }.toSet() }

    fun excluido(fecha: LocalDate): Flow<Boolean> = excluidos.map { fecha in it }

    suspend fun marcar(fecha: LocalDate, excluir: Boolean) {
        if (excluir) dao.excluir(DiaExcluido(fecha)) else dao.incluir(DiaExcluido(fecha))
    }
}

/** Objetivos de un día con los ajustes y el peso que había entonces. Null si aún no hay ajustes o pesos. */
fun observarObjetivosEn(fecha: LocalDate, ajustes: AjustesRepository, pesos: PesoRepository): Flow<ObjetivosDia?> =
    combine(ajustes.historial, pesos.historial) { historial, listaPesos -> objetivosEn(fecha, historial, listaPesos) }
