package com.example.simplechatapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lynq_notes")
data class LynqNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: String,       // Link note to specific chat/contact
    var title: String,

    // This will hold the JSON string of all canvas objects
    var canvasContentJson: String? = null,

    // The 'preview' could be a generated thumbnail path or a short text summary
    var preview: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),

    // Optional: Keep for very simple text-only notes or legacy data.
    // Consider migrating data from these to canvasContentJson if notes are edited.
    val legacyTextContent: String? = null,
    val legacyImagePath: String? = null,
    val legacyAudioPath: String? = null
)
