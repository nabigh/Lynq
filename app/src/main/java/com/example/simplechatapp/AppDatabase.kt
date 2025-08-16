package com.example.simplechatapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LynqNote::class], // Ensure LynqNote is listed here
    version = 1,                  // Increment version if you change schema later
    exportSchema = false          // Set to true if you want to export schema for migrations
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lynqNoteDao(): LynqNoteDao
    // If you have other DAOs for other entities, declare them here as abstract funcs
    // abstract fun otherDao(): OtherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Multiple threads can ask for the database at the same time, ensure we only initialize it once
            // by using synchronized. If INSTANCE is not null, then return it,
            // if it is, then create the database.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "simplechatapp_db" // Name of your database file
                )
                    // TODO: Add migration strategies here if you ever change the schema (version > 1)
                    // .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // Not recommended for production if data loss is an issue
                    // This will clear the database on schema version mismatch
                    // Replace with proper migrations for production apps.
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

