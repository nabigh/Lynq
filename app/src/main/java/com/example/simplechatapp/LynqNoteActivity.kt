package com.example.simplechatapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.simplechatapp.databinding.ActivityLynqNoteBinding

class LynqNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLynqNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLynqNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createNoteButton.setOnClickListener {
            // TODO: Open note editor
        }
    }
}
