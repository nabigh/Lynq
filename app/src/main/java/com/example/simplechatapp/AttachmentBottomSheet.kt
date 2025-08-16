package com.example.simplechatapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.simplechatapp.databinding.BottomSheetAttachmentBinding

class AttachmentBottomSheet(
    context: Context,
    private val onImageClick: () -> Unit,
    private val onDocumentClick: () -> Unit,
    private val onAudioClick: () -> Unit,
    private val onLynqNoteClick: () -> Unit
) : BottomSheetDialog(context) {

    private lateinit var binding: BottomSheetAttachmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BottomSheetAttachmentBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        binding.optionImage.setOnClickListener {
            onImageClick()
            dismiss()
        }
        binding.optionDocument.setOnClickListener {
            onDocumentClick()
            dismiss()
        }
        binding.optionAudio.setOnClickListener {
            onAudioClick()
            dismiss()
        }
        binding.optionLynqNote.setOnClickListener {
            onLynqNoteClick()
            dismiss()
        }
    }
}
