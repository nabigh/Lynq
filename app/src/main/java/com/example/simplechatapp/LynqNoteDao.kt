package com.example.simplechatapp

import androidx.room.*

@Dao
interface LynqNoteDao {
    @Query("SELECT * FROM lynq_notes WHERE contactId = :contactId ORDER BY updatedAt DESC")
    suspend fun getNotesForContact(contactId: String): List<LynqNote>

    /**
     * Retrieves a specific note by its unique ID.
     *
     * @param noteId The ID of the note to retrieve.
     * @return The LynqNote object if found, or null if no note with that ID exists.
     */
    @Query("SELECT * FROM lynq_notes WHERE id = :noteId") // Assuming 'id' is your primary key column name
    suspend fun getNoteById(noteId: Long): LynqNote? // Assuming 'id' is of type Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: LynqNote): Long

    @Update
    suspend fun update(note: LynqNote)

    @Delete
    suspend fun delete(note: LynqNote)
}

