package com.example.simplechatapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
// 1. Import the generated binding class for your layout
import com.example.simplechatapp.databinding.ActivityNotesListBinding
import kotlinx.coroutines.launch

class NotesListActivity : AppCompatActivity() {

    // 2. Declare a variable for the binding object
    private lateinit var binding: ActivityNotesListBinding
    private lateinit var adapter: NotesListAdapter
    private lateinit var db: AppDatabase // Assuming AppDatabase is your Room database class
    private var contactId: String? = null // Make it nullable to handle missing extra

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 3. Inflate the layout using the binding object
        binding = ActivityNotesListBinding.inflate(layoutInflater)
        // 4. Set the content view to the root of the binding object
        setContentView(binding.root)

        contactId = intent.getStringExtra("contactId")
        if (contactId == null) {
            // Handle the case where contactId is not passed, maybe finish the activity or show an error
            // For now, let's just log and return to prevent crashes later
            // Log.e("NotesListActivity", "contactId is null, finishing activity.")
            finish()
            return
        }

        // 5. Access views through the binding object
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize with an empty list and a click listener
        adapter = NotesListAdapter(emptyList()) { note -> // Use emptyList() for initialization
            val intent = Intent(this, NoteEditorActivity::class.java)
            intent.putExtra("noteId", note.id)
            intent.putExtra("contactId", contactId)
            startActivity(intent)
        }
        binding.notesRecyclerView.adapter = adapter

        // Access the FloatingActionButton through the binding object
        // The ID "addNoteFab" from your XML is converted to camelCase "addNoteFab" by View Binding
        binding.addNoteFab.setOnClickListener {
            val intent = Intent(this, NoteEditorActivity::class.java)
            intent.putExtra("contactId", contactId)
            // Consider adding a request code if you expect a result from NoteEditorActivity
            startActivity(intent)
        }

        // Initialize your database
        // Make sure AppDatabase and your DAO (lynqNoteDao) are correctly set up
        db = AppDatabase.getDatabase(this) // Replace AppDatabase with your actual database class name
    }

    override fun onResume() {
        super.onResume()
        // Load notes only if contactId is not null
        contactId?.let { loadNotes(it) }
    }

    private fun loadNotes(currentContactId: String) {
        lifecycleScope.launch {
            // Ensure db and its DAO are initialized
            // Make sure getNotesForContact is a suspend function or called from a coroutine scope
            val notes = db.lynqNoteDao().getNotesForContact(currentContactId)
            adapter.updateNotes(notes)
        }
    }
}
