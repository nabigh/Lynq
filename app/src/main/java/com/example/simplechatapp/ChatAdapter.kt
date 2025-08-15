package com.example.simplechatapp

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.concurrent.TimeUnit

class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userMessage: TextView = view.findViewById(R.id.userMessage)
        val userMedia: ImageView = view.findViewById(R.id.userMedia)
        val userDocument: TextView = view.findViewById(R.id.userDocument)
        val userAudioLayout: View = view.findViewById(R.id.userAudioLayout)
        val userAudioPlayButton: ImageView = view.findViewById(R.id.userAudioPlayButton)
        val userAudioDuration: TextView = view.findViewById(R.id.userAudioDuration)

        val botMessage: TextView = view.findViewById(R.id.botMessage)
        val botMedia: ImageView = view.findViewById(R.id.botMedia)
        val botDocument: TextView = view.findViewById(R.id.botDocument)
        val botAudioLayout: View = view.findViewById(R.id.botAudioLayout)
        val botAudioPlayButton: ImageView = view.findViewById(R.id.botAudioPlayButton)
        val botAudioDuration: TextView = view.findViewById(R.id.botAudioDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        // Hide all views first
        holder.userMessage.visibility = View.GONE
        holder.userMedia.visibility = View.GONE
        holder.userDocument.visibility = View.GONE
        holder.userAudioLayout.visibility = View.GONE

        holder.botMessage.visibility = View.GONE
        holder.botMedia.visibility = View.GONE
        holder.botDocument.visibility = View.GONE
        holder.botAudioLayout.visibility = View.GONE

        if (message.isSentByUser) {
            when (message.messageType) {
                MessageType.TEXT -> {
                    holder.userMessage.text = message.content
                    holder.userMessage.visibility = View.VISIBLE
                }
                MessageType.IMAGE, MessageType.VIDEO -> {
                    message.mediaUri?.let { uriString ->
                        holder.userMedia.visibility = View.VISIBLE
                        Glide.with(holder.userMedia.context)
                            .load(Uri.parse(uriString))
                            .into(holder.userMedia)
                    }
                }
                MessageType.DOCUMENT -> {
                    holder.userDocument.text = message.content ?: "Document"
                    holder.userDocument.visibility = View.VISIBLE
                }
                MessageType.AUDIO -> {
                    holder.userAudioLayout.visibility = View.VISIBLE
                    holder.userAudioDuration.text = getDurationString(message.audioDurationMs)

                    holder.userAudioPlayButton.setImageResource(android.R.drawable.ic_media_play)
                    holder.userAudioPlayButton.setOnClickListener {
                        message.mediaUri?.let { uriString ->
                            playAudio(Uri.parse(uriString), holder.userAudioPlayButton.context, holder.userAudioPlayButton)
                        }
                    }
                }
            }
        } else {
            when (message.messageType) {
                MessageType.TEXT -> {
                    holder.botMessage.text = message.content
                    holder.botMessage.visibility = View.VISIBLE
                }
                MessageType.IMAGE, MessageType.VIDEO -> {
                    message.mediaUri?.let { uriString ->
                        holder.botMedia.visibility = View.VISIBLE
                        Glide.with(holder.botMedia.context)
                            .load(Uri.parse(uriString))
                            .into(holder.botMedia)
                    }
                }
                MessageType.DOCUMENT -> {
                    holder.botDocument.text = message.content ?: "Document"
                    holder.botDocument.visibility = View.VISIBLE
                }
                MessageType.AUDIO -> {
                    holder.botAudioLayout.visibility = View.VISIBLE
                    holder.botAudioDuration.text = getDurationString(message.audioDurationMs)

                    holder.botAudioPlayButton.setImageResource(android.R.drawable.ic_media_play)
                    holder.botAudioPlayButton.setOnClickListener {
                        message.mediaUri?.let { uriString ->
                            playAudio(Uri.parse(uriString), holder.botAudioPlayButton.context, holder.botAudioPlayButton)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    private fun playAudio(uri: Uri, context: Context, playButton: ImageView) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            playButton.setImageResource(android.R.drawable.ic_media_play)
            return
        }

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            prepare()
            start()
            playButton.setImageResource(android.R.drawable.ic_media_pause)
            setOnCompletionListener {
                playButton.setImageResource(android.R.drawable.ic_media_play)
                release()
                mediaPlayer = null
            }
        }
    }

    private fun getDurationString(durationMs: Long?): String {
        if (durationMs == null || durationMs <= 0) return "00:00"
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}
