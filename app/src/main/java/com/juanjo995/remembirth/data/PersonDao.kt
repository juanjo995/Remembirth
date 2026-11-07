package com.juanjo995.remembirth.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): las operaciones sobre la tabla "persons".
 * Room genera la implementación en tiempo de compilación (KSP).
 */
@Dao
interface PersonDao {

    /** Inserta una persona y devuelve su id. "suspend": no bloquea la interfaz. */
    @Insert
    suspend fun insert(person: Person): Long

    /** Actualiza nombre y fecha de una persona existente (la busca por su id). */
    @Update
    suspend fun update(person: Person)

    /** Borra la persona (la busca por su id). */
    @Delete
    suspend fun delete(person: Person)

    /** Todas las personas por orden alfabético. El Flow emite de nuevo cada vez que la tabla cambia. */
    @Query("SELECT * FROM persons ORDER BY name COLLATE NOCASE")
    fun getAll(): Flow<List<Person>>

    /** Todas las personas, leídas una sola vez (para la comprobación diaria de notificaciones). */
    @Query("SELECT * FROM persons")
    suspend fun getAllOnce(): List<Person>
}
