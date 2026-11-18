package com.example.macros.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDate

/** Alimento con sus valores por 100 g. */
@Entity(tableName = "alimentos", indices = [Index(value = ["nombre"], unique = true)])
data class Alimento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val kcal: Double,
    val carbohidratos: Double,
    val proteinas: Double,
    val grasas: Double,
)

/** Un alimento apuntado en una comida (1 a 4) de un día. */
@Entity(
    tableName = "entradas",
    foreignKeys = [
        ForeignKey(
            entity = Alimento::class,
            parentColumns = ["id"],
            childColumns = ["alimentoId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("fecha"), Index("alimentoId")],
)
data class Entrada(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: LocalDate,
    val comida: Int,
    val alimentoId: Long,
    val gramos: Double,
)

/** Peso de un día: como mucho uno por fecha (la fecha es la clave). */
@Entity(tableName = "pesos")
data class Peso(
    @PrimaryKey val fecha: LocalDate,
    val kg: Double,
)

/** Entrada del diario junto con su alimento, para mostrar y calcular valores. */
data class EntradaConAlimento(
    @Embedded val entrada: Entrada,
    @Relation(parentColumn = "alimentoId", entityColumn = "id")
    val alimento: Alimento,
) {
    private val factor get() = entrada.gramos / 100.0
    val kcal get() = alimento.kcal * factor
    val carbohidratos get() = alimento.carbohidratos * factor
    val proteinas get() = alimento.proteinas * factor
    val grasas get() = alimento.grasas * factor
}
