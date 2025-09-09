package com.example.intervaltimer

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.intervaltimer.databinding.DialogAddBlockBinding
import kotlin.random.Random

class AddBlockDialog : DialogFragment() {
    interface OnBlockAddedListener {
        fun onBlockAdded(block: Block)
    }

    private var _binding: DialogAddBlockBinding? = null
    private val binding get() = _binding!!
    private var listener: OnBlockAddedListener? = null

    fun setListener(listener: OnBlockAddedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddBlockBinding.inflate(requireActivity().layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setTitle("Добавить блок")
            .setPositiveButton("Добавить") { dialog, which ->
                addBlock()
            }
            .setNegativeButton("Отмена", null)

        return builder.create()
    }

    private fun addBlock() {
        val name = binding.blockNameEditText.text.toString()
        val roundsText = binding.blockRoundsEditText.text.toString()

        if (name.isBlank() || roundsText.isBlank()) {
            return
        }

        val rounds = roundsText.toIntOrNull() ?: return

        if (rounds <= 0) {
            return
        }

        val block = Block(
            id = System.currentTimeMillis() + Random.nextLong(1000), // Более уникальный ID
            name = name,
            rounds = rounds,
            elements = emptyList(),
            duration = 0
        )

        listener?.onBlockAdded(block)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}