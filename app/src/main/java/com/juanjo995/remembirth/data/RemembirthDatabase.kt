package com.juanjo995.remembirth.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * La base de datos de la app (un fichero SQLite "remembirth.db" en el móvil).
 *
 * Si algún día cambias la estructura de [Person] (añadir un campo, etc.),
 * hay que subir "version" y escribir una migración.
 */
@Database(entities = [Person::class], version = 1, exportSchema = false)
abstract class RemembirthDatabase : RoomDatabase() {

    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var instance: RemembirthDatabase? = null

        /** Devuelve siempre la misma instancia (abrir la base de datos es caro). */
        fun getInstance(context: Context): RemembirthDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RemembirthDatabase::class.java,
                    "remembirth.db",
                ).build().also { instance = it }
            }
    }
}
