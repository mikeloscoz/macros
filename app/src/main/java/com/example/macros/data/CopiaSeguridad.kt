package com.example.macros.data

import androidx.room.withTransaction
import com.example.macros.data.db.Alimento
import com.example.macros.data.db.Entrada
import com.example.macros.data.db.MacrosDatabase
import com.example.macros.data.db.Peso
import com.example.macros.domain.Meta
import com.example.macros.domain.Sexo
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

/** Resumen de lo que hay en una copia, para informar al usuario. */
data class ResumenCopia(val alimentos: Int, val entradas: Int, val pesos: Int)

class CopiaInvalida(mensaje: String) : Exception(mensaje)

/**
 * Exporta e importa TODOS los datos (alimentos, incluidos los archivados; diario; pesos y ajustes) en JSON.
 * Importar sustituye todo lo que haya en el móvil.
 */
class CopiaSeguridad(private val db: MacrosDatabase, private val ajustes: AjustesRepository) {

    suspend fun exportar(): String {
        val a = ajustes.ajustes.first()
        val json = JSONObject()
            .put("app", "Macros")
            .put("version", VERSION)
            .put("exportado", LocalDateTime.now().toString())
            .put(
                "ajustes",
                JSONObject()
                    .put("mantenimiento", a.mantenimiento)
                    .put("meta", a.meta.name)
                    .put("sexo", a.sexo.name),
            )
            .put("alimentos", JSONArray().apply {
                db.alimentoDao().todos().forEach {
                    put(
                        JSONObject()
                            .put("id", it.id)
                            .put("nombre", it.nombre)
                            .put("kcal", it.kcal)
                            .put("carbohidratos", it.carbohidratos)
                            .put("proteinas", it.proteinas)
                            .put("grasas", it.grasas)
                            .put("archivado", it.archivado),
                    )
                }
            })
            .put("entradas", JSONArray().apply {
                db.entradaDao().todas().forEach {
                    put(
                        JSONObject()
                            .put("fecha", it.fecha.toString())
                            .put("comida", it.comida)
                            .put("alimentoId", it.alimentoId)
                            .put("gramos", it.gramos),
                    )
                }
            })
            .put("pesos", JSONArray().apply {
                db.pesoDao().todos().forEach {
                    put(JSONObject().put("fecha", it.fecha.toString()).put("kg", it.kg))
                }
            })
        return json.toString(2)
    }

    /** Lee y valida la copia entera ANTES de tocar nada; si todo está bien, sustituye los datos. */
    suspend fun importar(texto: String): ResumenCopia {
        val copia = leer(texto)
        db.withTransaction {
            db.entradaDao().borrarTodas()
            db.pesoDao().borrarTodos()
            db.alimentoDao().borrarTodos()
            db.alimentoDao().insertarTodos(copia.alimentos)
            db.entradaDao().insertarTodas(copia.entradas)
            db.pesoDao().guardarTodos(copia.pesos)
        }
        ajustes.guardar(copia.ajustes)
        // Tras importar no hay que volver a cargar el CSV inicial.
        ajustes.marcarAlimentosInicialesCargados()
        return ResumenCopia(copia.alimentos.size, copia.entradas.size, copia.pesos.size)
    }

    private class Copia(
        val ajustes: Ajustes,
        val alimentos: List<Alimento>,
        val entradas: List<Entrada>,
        val pesos: List<Peso>,
    )

    private fun leer(texto: String): Copia = try {
        val json = JSONObject(texto)
        if (json.optString("app") != "Macros") throw CopiaInvalida("El archivo no es una copia de Macros.")
        if (json.optInt("version", 0) > VERSION) throw CopiaInvalida("La copia es de una versión más nueva de la app.")

        val aj = json.getJSONObject("ajustes")
        val ajustesLeidos = Ajustes(
            mantenimiento = aj.getInt("mantenimiento"),
            meta = Meta.valueOf(aj.getString("meta")),
            sexo = Sexo.valueOf(aj.getString("sexo")),
        )
        val alimentos = json.getJSONArray("alimentos").objetos().map {
            Alimento(
                id = it.getLong("id"),
                nombre = it.getString("nombre"),
                kcal = it.getDouble("kcal"),
                carbohidratos = it.getDouble("carbohidratos"),
                proteinas = it.getDouble("proteinas"),
                grasas = it.getDouble("grasas"),
                archivado = it.optBoolean("archivado", false),
            )
        }
        val ids = alimentos.map { it.id }.toSet()
        if (ids.size != alimentos.size) throw CopiaInvalida("La copia tiene alimentos repetidos.")
        val entradas = json.getJSONArray("entradas").objetos().map {
            val entrada = Entrada(
                fecha = LocalDate.parse(it.getString("fecha")),
                comida = it.getInt("comida"),
                alimentoId = it.getLong("alimentoId"),
                gramos = it.getDouble("gramos"),
            )
            if (entrada.comida !in 1..4 || entrada.alimentoId !in ids) throw CopiaInvalida("La copia está dañada.")
            entrada
        }
        val pesos = json.getJSONArray("pesos").objetos().map {
            Peso(LocalDate.parse(it.getString("fecha")), it.getDouble("kg"))
        }
        Copia(ajustesLeidos, alimentos, entradas, pesos)
    } catch (e: CopiaInvalida) {
        throw e
    } catch (e: Exception) {
        throw CopiaInvalida("No se ha podido leer la copia: el archivo no es válido.")
    }

    private fun JSONArray.objetos(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

    private companion object {
        const val VERSION = 1
    }
}
