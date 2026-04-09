package com.example.frontend.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.frontend.data.database.dao.*
import com.example.frontend.data.database.entity.*


@Database(
    entities = [
        FestivalEntity::class,
        EditeurEntity::class,
        ReservationEntity::class,
        JeuFestivalEntity::class,
        ZoneTarifaireEntity::class,
        ZoneDuPlanEntity::class,
        TableJeuEntity::class,
        JeuTableEntity::class,
        ReservationTableEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FestivalDatabase : RoomDatabase() {
    abstract fun festivalDao(): FestivalDao
    abstract fun editeurDao(): EditeurDao
    abstract fun reservationDao(): ReservationDao
    abstract fun jeuFestivalDao(): JeuFestivalDao
    abstract fun zoneTarifaireDao(): ZoneTarifaireDao
    abstract fun zoneDuPlanDao(): ZoneDuPlanDao
    abstract fun tableJeuDao(): TableJeuDao
    abstract fun jeuTableDao(): JeuTableDao
    abstract fun reservationTableDao(): ReservationTableDao

    companion object {

        // @Volatile garantit que la valeur de Instance est toujours
        // lue depuis la mémoire principale (et pas depuis un cache de thread)
        @Volatile
        private var Instance: FestivalDatabase? = null

        fun getDatabase(context: Context): FestivalDatabase {
            // Si l'instance existe déjà on la retourne directement.
            // Sinon on cree une dans le bloc
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    FestivalDatabase::class.java,
                    "festival_cache"       // nom du fichier SQLite sur l'appareil
                )
                .fallbackToDestructiveMigration(dropAllTables = true) // si la version change, recrée la DB
                .build()
                .also { Instance = it }
            }
        }
    }
}
