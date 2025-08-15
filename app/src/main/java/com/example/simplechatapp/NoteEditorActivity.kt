package com.example.simplechatapp

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class NoteEditorActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDraw: Button
    private lateinit var btnImage: Button
    private lateinit var btnCamera: Button
    private lateinit var btnRecordAudio: Button
    private lateinit var btnPlayAudio: Button
    private lateinit var btnSpeechToText: Button
    private lateinit var btnTextToSpeech: Button
    private lateinit var drawingCanvas: DrawingView

    private lateinit var db: AppDatabase
    private var noteId: Long? = null
    private lateinit var contactId: String

    private var imagePath: String? = null
    private var audioPath: String? = null
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var tts: TextToSpeech? = null

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ),
            1
        )

        contactId = intent.getStringExtra("contactId") ?: return
        noteId = intent.getLongExtra("noteId", -1L).takeIf { it != -1L }

        editTitle = findViewById(R.id.editNoteTitle)
        editContent = findViewById(R.id.editNoteContent)
        btnSave = findViewById(R.id.btnSaveNote)
        btnDraw = findViewById(R.id.btnDraw)
        btnImage = findViewById(R.id.btnImage)
        btnCamera = findViewById(R.id.btnCamera)
        btnRecordAudio = findViewById(R.id.btnRecordAudio)
        btnPlayAudio = findViewById(R.id.btnPlayAudio)
        btnSpeechToText = findViewById(R.id.btnSpeechToText)
        btnTextToSpeech = findViewById(R.id.btnTextToSpeech)
        drawingCanvas = findViewById(R.id.drawingCanvas)

        db = AppDatabase.getDatabase(this)
        tts = TextToSpeech(this, this)

        if (noteId != null) loadNote()

        // Gallery picker
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                imagePath = uri.toString()
                Toast.makeText(this, "Image added", Toast.LENGTH_SHORT).show()
            }
        }

        // Camera capture
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    imagePath = saveBitmapToFile(it)
                    Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }

        btnCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }

        btnDraw.setOnClickListener {
            val bitmap = drawingCanvas.getBitmap()
            imagePath = saveBitmapToFile(bitmap)
            Toast.makeText(this, "Drawing saved", Toast.LENGTH_SHORT).show()
        }

        btnRecordAudio.setOnClickListener {
            if (recorder == null) startRecording() else stopRecording()
        }

        btnPlayAudio.setOnClickListener {
            playAudio()
        }

        btnSpeechToText.setOnClickListener {
            startSpeechToText()
        }

        btnTextToSpeech.setOnClickListener {
            val text = editContent.text.toString()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }

        btnSave.setOnClickListener {
            saveNote()
        }
    }

    private fun loadNote() {
        lifecycleScope.launch {
            val note = db.lynqNoteDao().getNotesForContact(contactId).find { it.id == noteId }
            note?.let {
                editTitle.setText(it.title)
                editContent.setText(it.content)
                imagePath = it.imagePath
                audioPath = it.audioPath
            }
        }
    }

    private fun saveNote() {
        lifecycleScope.launch {
            val note = LynqNote(
                id = noteId ?: 0,
                contactId = contactId,
                title = editTitle.text.toString(),
                content = editContent.text.toString(),
                preview = editContent.text.toString().take(50),
                imagePath = imagePath,
                audioPath = audioPath
            )
            if (noteId == null) db.lynqNoteDao().insert(note) else db.lynqNoteDao().update(note)
            finish()
        }
    }

    private fun startRecording() {
        audioPath = "${externalCacheDir?.absolutePath}/note_audio_${System.currentTimeMillis()}.3gp"
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioPath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
                Toast.makeText(this@NoteEditorActivity, "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show()
    }

    private fun playAudio() {
        if (audioPath != null) {
            player = MediaPlayer().apply {
                try {
                    setDataSource(audioPath)
                    prepare()
                    start()
                    Toast.makeText(this@NoteEditorActivity, "Playing audio", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        try {
            startActivityForResult(intent, 200)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            editContent.append(" " + result?.get(0))
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "drawing_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.shutdown()
        player?.release()
        recorder?.release()
    }
}
