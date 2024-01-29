package jatx.common

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SmokeEventEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smokingDao(): SmokingDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context): AppDatabase = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                Log.e("db", "building")
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "smoking.db"
        )
            .allowMainThreadQueries()
            .build()
    }
}