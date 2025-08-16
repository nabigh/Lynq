package com.example.simplechatapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.setText
import androidx.compose.ui.semantics.text
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.simplechatapp.databinding.ActivityNoteEditorBinding // Make sure your layout file is named activity_note_editor.xml
import kotlinx.coroutines.launch
import java.util.* // For UUID if you were to use it directly here, though it's in CanvasObject now

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditorBinding
    private lateinit var db: AppDatabase
    private var currentNoteId: Long? = null
    private lateinit var currentContactId: String // Ensure this is always passed and valid

    // Example launcher for requesting permissions if needed later
    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGranted = true
            permissions.entries.forEach {
                if (!it.value) {
                    allGranted = false
                    // Handle individual permission denial if necessary
                    // Toast.makeText(this, "${it.key} permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            if (allGranted) {
                // Permissions granted, proceed with action that required them
                // Example: Toast.makeText(this, "All required permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions were denied. Certain features might not work.", Toast.LENGTH_LONG).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Essential: Get contactId and noteId ---
        currentContactId = intent.getStringExtra("contactId") ?: run {
            Toast.makeText(this, "Error: Contact ID missing. Cannot open note editor.", Toast.LENGTH_LONG).show()
            finish() // Critical to finish if contactId is missing
            return
        }

        intent.getLongExtra("noteId", -1L).takeIf { it != -1L }?.let {
            currentNoteId = it
        }

        db = AppDatabase.getDatabase(this)

        if (currentNoteId != null) {
            loadNoteData()
        } else {
            // For new notes, ensure canvas is clear (though it should be by default)
            binding.interactiveCanvas.clearCanvas()
        }

        setupToolbar()
        setupToolButtons()

        binding.btnSaveNote.setOnClickListener {
            saveNoteData()
        }

        // Example: Check and request necessary permissions (uncomment and modify as needed)
        // checkAndRequestPermissions()
    }

    private fun setupToolbar() {
        // You can set up a SupportActionBar here if your theme supports it
        // supportActionBar?.title = if (currentNoteId == null) "New Note" else "Edit Note"
        // supportActionBar?.setDisplayHomeAsUpEnabled(true) // For back navigation
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle Toolbar back button
//        if (item.itemId == android.R.id.home) {
//            // Consider prompting to save if there are unsaved changes
//            finish()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = mutableListOf<String>()
        // Add permissions your canvas might need (e.g., for adding images from gallery/camera)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE) // Use READ_MEDIA_IMAGES for API 33+
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // requiredPermissions.add(Manifest.permission.CAMERA)
        }
        // Add other permissions like RECORD_AUDIO if you add those features to the canvas

        if (requiredPermissions.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(requiredPermissions.toTypedArray())
        }
    }


    private fun setupToolButtons() {
        binding.btnSetColorRed.setOnClickListener {
            binding.interactiveCanvas.setPenColor(Color.RED)
            // Consider visual feedback for selected tool/color
        }
        binding.btnSetColorBlack.setOnClickListener {
            binding.interactiveCanvas.setPenColor(Color.BLACK)
        }
        // Add more colors
        binding.btnSetColorBlue.setOnClickListener { // Assuming you add this button to XML
            binding.interactiveCanvas.setPenColor(Color.BLUE)
        }

        binding.btnSetStrokeSmall.setOnClickListener {
            binding.interactiveCanvas.setPenStrokeWidth(8f) // Adjusted for better visibility
        }
        binding.btnSetStrokeMedium.setOnClickListener { // Assuming you add this button to XML
            binding.interactiveCanvas.setPenStrokeWidth(16f)
        }
        binding.btnSetStrokeLarge.setOnClickListener {
            binding.interactiveCanvas.setPenStrokeWidth(32f)
        }
        binding.btnClearCanvas.setOnClickListener {
            binding.interactiveCanvas.clearCanvas()
        }

        // --- TODO: Future Tool Implementations ---
        // binding.btnEraser.setOnClickListener {
        //      binding.interactiveCanvas.setMode(InteractiveCanvasView.Mode.ERASE)
        //      binding.interactiveCanvas.setEraserSize(20f) // Example
        // }
        // binding.btnAddText.setOnClickListener {
        //      binding.interactiveCanvas.setMode(InteractiveCanvasView.Mode.TEXT_ADD)
        //      // Could open a dialog to input text, then pass to canvas
        // }
        // binding.btnAddImage.setOnClickListener {
        //      binding.interactiveCanvas.setMode(InteractiveCanvasView.Mode.IMAGE_ADD)
        //      // Launch gallery/camera, then pass Uri to canvas
        // }
    }

    private fun loadNoteData() {
        currentNoteId?.let { noteId ->
            lifecycleScope.launch {
                val note = db.lynqNoteDao().getNoteById(noteId)
                if (note != null) {
                    binding.editNoteTitle.setText(note.title)
                    binding.interactiveCanvas.loadContentFromJson(note.canvasContentJson)
                    // TODO: Load other relevant fields from the note if necessary
                    // e.g., if you store canvas zoom/pan state, background color, etc.
                } else {
                    Toast.makeText(this@NoteEditorActivity, "Failed to load note.", Toast.LENGTH_SHORT).show()
                    // Optionally finish if note can't be found
                    // finish()
                }
            }
        }
    }

    private fun saveNoteData() {
        val title = binding.editNoteTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            binding.editNoteTitle.error = "Title required" // More direct feedback
            return
        }

        val canvasJson = binding.interactiveCanvas.getContentAsJson()
        // If canvasJson is null (empty canvas), you might want to store it as such or as an empty string ""
        // depending on how your loadContentFromJson handles null vs empty.

        lifecycleScope.launch {
            val timestamp = System.currentTimeMillis()
            var noteToSave: LynqNote? = null

            if (currentNoteId != null) {
                // Existing note
                noteToSave = db.lynqNoteDao().getNoteById(currentNoteId!!)?.apply {
                    this.title = title
                    this.canvasContentJson = canvasJson
                    this.updatedAt = timestamp
                    this.preview = generatePreview(canvasJson, title) // Pass content for preview
                }
                if (noteToSave == null) {
                    Toast.makeText(this@NoteEditorActivity, "Error: Could not find existing note to update.", Toast.LENGTH_LONG).show()
                    return@launch
                }
            } else {
                // New note
                noteToSave = LynqNote(
                    contactId = currentContactId,
                    title = title,
                    canvasContentJson = canvasJson,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                    preview = generatePreview(canvasJson, title) // Pass content for preview
                )
            }

            // Perform the database operation
            noteToSave?.let {
                try {
                    if (it.id == 0L) { // id is 0 for a new, unsaved entity
                        val newId = db.lynqNoteDao().insert(it) // Assuming insert returns the new ID
                        if (newId > 0) {
                            currentNoteId = newId // Update currentNoteId if it was a new note
                            Toast.makeText(this@NoteEditorActivity, "Note Saved", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@NoteEditorActivity, "Failed to save new note.", Toast.LENGTH_SHORT).show()
                        }
                    } else { // Existing note
                        db.lynqNoteDao().update(it)
                        Toast.makeText(this@NoteEditorActivity, "Note Updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@NoteEditorActivity, "Error saving note: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Generates a preview for the note.
     * This is a placeholder. You'll need a more sophisticated implementation.
     * For example, get first few lines of text from canvas objects, or a thumbnail.
     */
    private fun generatePreview(canvasJson: String?, title: String): String {
        // Simplistic preview:
        if (canvasJson != null && canvasJson.length > 50) { // Arbitrary check if there's significant canvas content
            return "$title (Drawing)"
        }
        return title.take(100) // Default to title if no complex canvas content detected by this simple check
    }


    override fun onDestroy() {
        super.onDestroy()
        // Release any other resources here if necessary
        // e.g., if InteractiveCanvasView held onto large bitmaps directly (though it shouldn't for long)
    }
}
