package com.example.simplechatapp

data class Message(
    val content: String?,
    val mediaUri: String?, // Uri as String for media like images, audio, video
    val isSentByUser: Boolean,
    val messageType: MessageType,
    val audioDurationMs: Long? = null  // duration for audio messages
)

enum class MessageType {
    TEXT, IMAGE, VIDEO, DOCUMENT, AUDIO
}