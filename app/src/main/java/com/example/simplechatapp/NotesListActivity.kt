package com.example.simplechatapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class NotesListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotesListAdapter
    private lateinit var db: AppDatabase
    private lateinit var contactId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes_list)

        contactId = intent.getStringExtra("contactId") ?: return

        recyclerView = findViewById(R.id.notesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = NotesListAdapter(listOf()) { note ->
            val intent = Intent(this, NoteEditorActivity::class.java)
            intent.putExtra("noteId", note.id)
            intent.putExtra("contactId", contactId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddNote)
        fabAdd.setOnClickListener {
            val intent = Intent(this, NoteEditorActivity::class.java)
            intent.putExtra("contactId", contactId)
            startActivity(intent)
        }

        db = AppDatabase.getDatabase(this)
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            val notes = db.lynqNoteDao().getNotesForContact(contactId)
            adapter.updateNotes(notes)
        }
    }
}
