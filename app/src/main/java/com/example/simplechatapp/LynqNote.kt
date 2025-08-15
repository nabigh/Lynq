package com.example.simplechatapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lynq_notes")
data class LynqNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: String,       // Link note to specific chat/contact
    val title: String,
    val content: String?,        // Text content
    val preview: String?,        // Small preview shown in list
    val imagePath: String?,      // If an image is attached
    val audioPath: String?,      // If audio note recorded
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
