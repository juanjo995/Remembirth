package com.juanjo995.remembirth.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Una persona guardada. Cada instancia es una fila de la tabla "persons".
 */
@Entity(tableName = "persons")
data class Person(
    /** Lo genera la base de datos al insertar (1, 2, 3...). */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    /** Fecha de nacimiento: milisegundos UTC de la medianoche de ese día. */
    val birthDateMillis: Long,
)
