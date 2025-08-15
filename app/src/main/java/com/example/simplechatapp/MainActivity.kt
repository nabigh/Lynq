package com.example.simplechatapp

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var camButton: Button
    private lateinit var attachButton: Button
    private lateinit var recButton: Button

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter

    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var attachLauncher: ActivityResultLauncher<String>
    private lateinit var requestAudioPermissionLauncher: ActivityResultLauncher<String>

    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false

    private val PICK_FILE_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        camButton = findViewById(R.id.camButton)
        attachButton = findViewById(R.id.attachButton)
        recButton = findViewById(R.id.recButton)

        adapter = ChatAdapter(messages)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = adapter

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString()
            if (messageText.isNotBlank()) {
                messages.add(
                    Message(
                        content = messageText,
                        mediaUri = null,
                        isSentByUser = true,
                        messageType = MessageType.TEXT
                    )
                )
                messages.add(
                    Message(
                        content = "Echo: $messageText",
                        mediaUri = null,
                        isSentByUser = false,
                        messageType = MessageType.TEXT
                    )
                )
                adapter.notifyItemRangeInserted(messages.size - 2, 2)
                chatRecyclerView.scrollToPosition(messages.size - 1)
                messageEditText.text.clear()
            }
        }

        // Camera permission request launcher
        requestCameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    launchCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }

        // Camera intent launcher
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                messages.add(
                    Message(
                        content = null,
                        mediaUri = photoUri.toString(),
                        isSentByUser = true,
                        messageType = MessageType.IMAGE
                    )
                )
                adapter.notifyItemInserted(messages.size - 1)
                chatRecyclerView.scrollToPosition(messages.size - 1)
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }

        camButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    launchCamera()
                }
                else -> {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        // File attachment launcher
        attachLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    // You can enhance this by detecting MIME types and message types
                    messages.add(
                        Message(
                            content = null,
                            mediaUri = uri.toString(),
                            isSentByUser = true,
                            messageType = MessageType.DOCUMENT
                        )
                    )
                    adapter.notifyItemInserted(messages.size - 1)
                    chatRecyclerView.scrollToPosition(messages.size - 1)
                }
            }

        attachButton.setOnClickListener {
            // Allow user to pick any file type, or restrict to images/videos/documents
            attachLauncher.launch("*/*")
        }

        // Audio recording permission launcher
        requestAudioPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    toggleAudioRecording()
                } else {
                    Toast.makeText(this, "Audio recording permission denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        recButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    toggleAudioRecording()
                }
                else -> {
                    requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
            cameraLauncher.launch(photoUri)
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating file for photo", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir("Pictures")
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun toggleAudioRecording() {
        if (isRecording) {
            stopRecording()
            recButton.text = "Record"
        } else {
            startRecording()
            recButton.text = "Stop"
        }
        isRecording = !isRecording
    }

    private fun startRecording() {
        try {
            audioFilePath = createAudioFile().absolutePath
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()

            audioFilePath?.let { path ->
                messages.add(
                    Message(
                        content = null,
                        mediaUri = Uri.fromFile(File(path)).toString(),
                        isSentByUser = true,
                        messageType = MessageType.AUDIO
                    )
                )
                adapter.notifyItemInserted(messages.size - 1)
                chatRecyclerView.scrollToPosition(messages.size - 1)
            }
        } catch (e: RuntimeException) {
            Toast.makeText(this, "Failed to stop recording: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createAudioFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir("Audio")
        return File.createTempFile(
            "AUD_${timeStamp}_",
            ".m4a",
            storageDir
        )
    }
}
