package com.example.simplechatapp


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LynqNote::class], version = 1)
abstract class LynqDatabase : RoomDatabase() {
    abstract fun lynqNoteDao(): LynqNoteDao

    companion object {
        @Volatile private var INSTANCE: LynqDatabase? = null

        fun getDatabase(context: Context): LynqDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LynqDatabase::class.java,
                    "lynq.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
