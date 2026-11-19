package com.example.macros.ui.ajustes

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macros.data.Ajustes
import com.example.macros.data.AppContainer
import com.example.macros.data.CopiaInvalida
import com.example.macros.domain.Meta
import com.example.macros.domain.Objetivos
import com.example.macros.domain.Sexo
import com.example.macros.domain.leerCsvAlimentos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

data class AjustesUiState(
    val cargando: Boolean = true,
    val ajustes: Ajustes = Ajustes(),
    val peso: Double? = null,
    val objetivos: Objetivos? = null,
)

class AjustesViewModel(private val c: AppContainer) : ViewModel() {

    val estado: StateFlow<AjustesUiState> =
        combine(c.ajustes.ajustes, c.pesos.actual, c.objetivos) { a, peso, o ->
            AjustesUiState(cargando = false, ajustes = a, peso = peso?.kg, objetivos = o)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AjustesUiState())

    /** Mensajes cortos para mostrar abajo (snackbar). */
    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val mensajes: SharedFlow<String> = _mensajes

    // --- Mis datos ---

    /** Cambiar el peso actual = registrar el peso de hoy. */
    fun guardarPeso(kg: Double) {
        viewModelScope.launch { c.pesos.registrar(kg) }
    }

    fun guardarMantenimiento(kcal: Int) = editar { it.copy(mantenimiento = kcal) }
    fun elegirMeta(meta: Meta) = editar { it.copy(meta = meta) }
    fun elegirSexo(sexo: Sexo) = editar { it.copy(sexo = sexo) }

    private fun editar(cambio: (Ajustes) -> Ajustes) {
        viewModelScope.launch { c.ajustes.guardar(cambio(c.ajustes.ajustes.first())) }
    }

    // --- Copia de seguridad ---

    fun exportar(uri: Uri) = tarea("No se ha podido guardar la copia.") {
        val texto = c.copias.exportar()
        withContext(Dispatchers.IO) {
            c.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use { it.write(texto) }
                ?: error("Sin acceso al archivo")
        }
        "Copia guardada."
    }

    fun importar(uri: Uri) = tarea("No se ha podido importar la copia.") {
        val r = c.copias.importar(leer(uri))
        "Copia importada: ${r.alimentos} alimentos, ${r.entradas} apuntes y ${r.pesos} pesos."
    }

    fun importarCsv(uri: Uri) = tarea("No se ha podido leer el CSV.") {
        val leidos = leerCsvAlimentos(leer(uri))
        if (leidos.isEmpty()) return@tarea "El archivo no tiene alimentos válidos."
        val nuevos = c.alimentos.insertarSiNoExisten(leidos)
        val repetidos = leidos.size - nuevos
        buildString {
            append(if (nuevos == 1) "Se ha añadido 1 alimento" else "Se han añadido $nuevos alimentos")
            if (repetidos > 0) append(" ($repetidos ya existían)")
            append('.')
        }
    }

    /** Lee el archivo como UTF-8; si no lo es (CSV guardado con Excel en Windows), como Windows-1252. */
    private suspend fun leer(uri: Uri): String = withContext(Dispatchers.IO) {
        val bytes = c.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Sin acceso al archivo")
        try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (_: CharacterCodingException) {
            String(bytes, Charset.forName("windows-1252"))
        }
    }

    /** Ejecuta una tarea y muestra su resultado, o un mensaje de error si falla. */
    private fun tarea(mensajeError: String, bloque: suspend () -> String) {
        viewModelScope.launch {
            val mensaje = try {
                bloque()
            } catch (e: CopiaInvalida) {
                e.message ?: mensajeError
            } catch (e: Exception) {
                Log.e("Macros", mensajeError, e)
                mensajeError
            }
            _mensajes.emit(mensaje)
        }
    }
}
