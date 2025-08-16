package com.example.simplechatapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplechatapp.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val messages = mutableListOf<Message>()
    private lateinit var chatAdapter: ChatAdapter

    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var attachLauncher: ActivityResultLauncher<String>
    private lateinit var requestAudioPermissionLauncher: ActivityResultLauncher<String>

    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupTextWatcher()
        setupButtonListeners() // This will now also call setupEmojiPicker
        setupLaunchers()
        updateInputAreaUI(binding.messageEditText.text.toString())
        setupSystemBackButtonHandler() // Setup back button handling
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.chatRecyclerView.adapter = chatAdapter
    }

    private fun setupTextWatcher() {
        binding.messageEditText.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateInputAreaUI(s.toString())
            }
        })
    }

    private fun updateInputAreaUI(text: String) {
        val textEmpty = text.trim().isEmpty()
        binding.sendOrRecordButton.setImageResource(if (textEmpty) R.drawable.mic else R.drawable.send)
        binding.cameraButton.visibility = if (textEmpty) View.VISIBLE else View.GONE
        binding.attachButton.visibility = View.VISIBLE
    }

    private fun setupButtonListeners() {
        binding.sendOrRecordButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isEmpty()) {
                if (binding.emojiPickerView.isVisible) {
                    hideEmojiPicker() // Ensure picker is hidden
                }
                toggleAudioRecording()
            } else {
                sendMessage(messageText)
                binding.messageEditText.text.clear()
                if (binding.emojiPickerView.isVisible) {
                    hideEmojiPicker() // Ensure picker is hidden
                }
            }
        }

        binding.cameraButton.setOnClickListener {
            if (binding.emojiPickerView.isVisible) {
                hideEmojiPicker()
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.attachButton.setOnClickListener {
            if (binding.emojiPickerView.isVisible) {
                hideEmojiPicker()
            }
            val sheet = AttachmentBottomSheet(
                this,
                onImageClick = { /* Open gallery */ },
                onDocumentClick = { /* Open document picker */ },
                onAudioClick = { /* Start audio recording */ },
                onLynqNoteClick = {
                    startActivity(Intent(this, LynqNoteActivity::class.java))
                }
            )
            sheet.show()
        }

        binding.emojiButton.setOnClickListener {
            toggleEmojiPickerAndKeyboard()
        }

        setupEmojiPicker()
    }

    private fun setupEmojiPicker() {
        binding.emojiPickerView.setOnEmojiPickedListener { emojiViewItem ->
            val emoji = emojiViewItem.emoji
            binding.messageEditText.append(emoji)
        }

        binding.messageEditText.setOnClickListener {
            if (binding.emojiPickerView.isVisible) {
                hideEmojiPicker()
                showKeyboard(binding.messageEditText)
            }
        }
        binding.messageEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.emojiPickerView.isVisible) {
                hideEmojiPicker()
                // Keyboard should show automatically due to focus if not already shown
                // but explicitly calling it ensures it if another view took focus away from keyboard.
                showKeyboard(binding.messageEditText)
            }
        }
    }

    private fun toggleEmojiPickerAndKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (binding.emojiPickerView.isVisible) {
            hideEmojiPicker()
            showKeyboard(binding.messageEditText)
        } else {
            if (imm.isActive(binding.messageEditText)) {
                hideKeyboard(binding.messageEditText)
                binding.root.postDelayed({ showEmojiPicker() }, 100)
            } else {
                showEmojiPicker()
            }
        }
    }

    private fun showEmojiPicker() {
        if (!binding.emojiPickerView.isVisible) {
            binding.emojiPickerView.visibility = View.VISIBLE
            // binding.emojiButton.setImageResource(R.drawable.ic_keyboard) // Optional: change icon
        }
    }

    private fun hideEmojiPicker() {
        if (binding.emojiPickerView.isVisible) {
            binding.emojiPickerView.visibility = View.GONE
            // binding.emojiButton.setImageResource(R.drawable.emoji_28) // Optional: change icon back
        }
    }

    private fun showKeyboard(view: View) {
        if (view.requestFocus()) { // Request focus before showing keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupSystemBackButtonHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.emojiPickerView.isVisible) {
                    hideEmojiPicker()
                    // If you want keyboard to appear when back is pressed from emoji picker
                    // showKeyboard(binding.messageEditText)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed() // Default back behavior
                    isEnabled = true
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setupLaunchers() {
        requestCameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) launchCamera() else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                messages.add(Message(null, photoUri.toString(), true, MessageType.IMAGE))
                chatAdapter.notifyItemInserted(messages.size - 1)
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }

        attachLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                messages.add(Message(null, uri.toString(), true, MessageType.DOCUMENT))
                chatAdapter.notifyItemInserted(messages.size - 1)
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
            }
        }

        requestAudioPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) toggleAudioRecording() else Toast.makeText(this, "Audio permission denied", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendMessage(text: String) {
        messages.add(Message(text, null, true, MessageType.TEXT))
        messages.add(Message("Echo: $text", null, false, MessageType.TEXT)) // Simulate receiving echoed message
        chatAdapter.notifyItemRangeInserted(messages.size - 2, 2)
        binding.chatRecyclerView.scrollToPosition(messages.size - 1)
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
            cameraLauncher.launch(photoUri)
        } catch (e: IOException) {
            Toast.makeText(this, "Error creating file for photo: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir("Pictures") ?: throw IOException("External storage not available")
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw IOException("Failed to create 'Pictures' directory")
        }
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun toggleAudioRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        try {
            val audioFile = createAudioFile()
            audioFilePath = audioFile.absolutePath
            mediaRecorder = MediaRecorder(this).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }
            isRecording = true
            binding.sendOrRecordButton.setImageResource(R.drawable.pause_24)
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) { // Catch generic Exception for more robustness
            isRecording = false
            updateInputAreaUI(binding.messageEditText.text.toString())
            Toast.makeText(this, "Recording failed to start: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if (!isRecording) return // Should not happen if logic is correct
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            isRecording = false // Set state before potential message sending
            audioFilePath?.let { path ->
                val file = File(path)
                if (file.exists() && file.length() > 0) { // Check if file is valid
                    messages.add(Message(null, Uri.fromFile(file).toString(), true, MessageType.AUDIO))
                    chatAdapter.notifyItemInserted(messages.size - 1)
                    binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                } else {
                    Toast.makeText(this, "Recording was empty or failed to save.", Toast.LENGTH_SHORT).show()
                    file.delete() // Clean up empty/corrupt file
                }
            }
        } catch (e: Exception) { // Catch generic Exception
            isRecording = false // Reset state on failure
            Toast.makeText(this, "Failed to stop recording: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            audioFilePath?.let { File(it).delete() } // Clean up potentially corrupt file
        } finally {
            mediaRecorder = null
            audioFilePath = null
            updateInputAreaUI(binding.messageEditText.text.toString()) // Always update UI
        }
    }

    @Throws(IOException::class)
    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDirName = "Audio"
        val storageDir = getExternalFilesDir(storageDirName)
            ?: throw IOException("External storage not available for $storageDirName")
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw IOException("Failed to create '$storageDirName' directory")
        }
        return File.createTempFile("AUD_${timeStamp}_", ".m4a", storageDir)
    }

    abstract class SimpleTextWatcher : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }
}
