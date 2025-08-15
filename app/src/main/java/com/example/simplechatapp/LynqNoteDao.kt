package com.example.simplechatapp

import androidx.room.*

@Dao
interface LynqNoteDao {
    @Query("SELECT * FROM lynq_notes WHERE contactId = :contactId ORDER BY updatedAt DESC")
    suspend fun getNotesForContact(contactId: String): List<LynqNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: LynqNote): Long

    @Update
    suspend fun update(note: LynqNote)

    @Delete
    suspend fun delete(note: LynqNote)
}
