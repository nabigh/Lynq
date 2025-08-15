package com.example.simplechatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotesListAdapter(
    private var notes: List<LynqNote>,
    private val onNoteClick: (LynqNote) -> Unit
) : RecyclerView.Adapter<NotesListAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.noteTitle)
        val previewTextView: TextView = itemView.findViewById(R.id.notePreview)

        init {
            itemView.setOnClickListener {
                val note = notes[adapterPosition]
                onNoteClick(note)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.previewTextView.text = note.preview ?: ""
    }

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<LynqNote>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}
