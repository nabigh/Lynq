package com.example.simplechatapp

import android.net.Uri // Added import for Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return messages[position].messageType.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val type = MessageType.values()[viewType]
        return when (type) {
            MessageType.TEXT -> TextMessageViewHolder(inflater.inflate(R.layout.item_text_message, parent, false))
            MessageType.IMAGE -> ImageMessageViewHolder(inflater.inflate(R.layout.item_image_message, parent, false))
            MessageType.AUDIO -> AudioMessageViewHolder(inflater.inflate(R.layout.item_audio_message, parent, false))
            MessageType.DOCUMENT -> DocumentMessageViewHolder(inflater.inflate(R.layout.item_document_message, parent, false))
            MessageType.VIDEO -> VideoMessageViewHolder(inflater.inflate(R.layout.item_video_message, parent, false)) // Added VIDEO case
            // Consider adding an else branch for future-proofing if new types are added frequently
            // else -> throw IllegalArgumentException("Unsupported message type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is TextMessageViewHolder -> holder.bind(message)
            is ImageMessageViewHolder -> holder.bind(message)
            is AudioMessageViewHolder -> holder.bind(message)
            is DocumentMessageViewHolder -> holder.bind(message)
            is VideoMessageViewHolder -> holder.bind(message) // Added VIDEO case
        }
    }

    override fun getItemCount(): Int = messages.size

    // --- ViewHolder classes ---
    class TextMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.messageTextView)
        fun bind(message: Message) { textView.text = message.content }
    }

    class ImageMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.messageImageView)
        fun bind(message: Message) {
            // Make sure message.mediaUri is not null and is a valid URI
            message.mediaUri?.let { imageView.setImageURI(Uri.parse(it)) }
        }
    }

    class AudioMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val audioLabel: TextView = view.findViewById(R.id.audioLabel)
        fun bind(message: Message) { audioLabel.text = "Audio: ${message.mediaUri}" }
    }

    class DocumentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val docLabel: TextView = view.findViewById(R.id.documentLabel)
        fun bind(message: Message) { docLabel.text = "Document: ${message.mediaUri}" }
    }

    // You'll need to create a ViewHolder for Video messages
    class VideoMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Example: a TextView to show the video URI, or a VideoView
        private val videoLabel: TextView = view.findViewById(R.id.videoLabel) // Assuming you have a videoLabel in your layout
        fun bind(message: Message) { videoLabel.text = "Video: ${message.mediaUri}" }
    }
}
