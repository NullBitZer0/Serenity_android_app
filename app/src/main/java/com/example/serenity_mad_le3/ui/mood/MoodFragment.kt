package com.example.serenity_mad_le3.ui.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serenity_mad_le3.R
import com.example.serenity_mad_le3.data.Prefs
import com.example.serenity_mad_le3.model.Mood
import com.example.serenity_mad_le3.util.EmojiMapper
import com.google.android.material.button.MaterialButton

class MoodFragment : Fragment() {
    private lateinit var prefs: Prefs
    private lateinit var adapter: MoodHistoryAdapter

    private val emojiChoices: List<String> by lazy { EmojiMapper.allEmojis }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())

        // Keep the history list reverse-chronological so the latest mood is easy to find.
        val list = prefs.getMoods().apply { sortByDescending { it.timestamp } }
        adapter = MoodHistoryAdapter(list)
        val rv = view.findViewById<RecyclerView>(R.id.recyclerMoods)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val note = view.findViewById<EditText>(R.id.editNote)

        view.findViewById<MaterialButton>(R.id.btnSaveMood).setOnClickListener {
            val noteText = note.text.toString().trim()
            if (noteText.isEmpty()) {
                // Nudge the user to add context so the trend chart has richer data.
                note.error = getString(R.string.mood_note_required)
                note.requestFocus()
            } else {
                showEmojiPicker(note, noteText)
            }
        }

        view.findViewById<MaterialButton>(R.id.btnViewChart).setOnClickListener {
            // Push the mood analytics fragment via the nav graph.
            findNavController().navigate(R.id.action_mood_to_chart)
        }
    }

    private fun showEmojiPicker(noteField: EditText, noteText: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_emoji_grid, null)
        val grid = dialogView.findViewById<GridLayout>(R.id.layoutEmojiGrid)

        var alert: AlertDialog? = null
        emojiChoices.forEach { emoji ->
            val button = LayoutInflater.from(requireContext()).inflate(R.layout.item_emoji_option, grid, false) as MaterialButton
            button.text = emoji
            button.contentDescription = getString(R.string.emoji_option_description, emoji)
            button.setOnClickListener {
                saveMoodEntry(emoji, noteText)
                noteField.text.clear()
                noteField.error = null
                // Close the dialog once a selection is made so the flow feels immediate.
                alert?.dismiss()
            }
            grid.addView(button)
        }

        alert = AlertDialog.Builder(requireContext())
            .setTitle(R.string.choose_emoji)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel, null)
            .create()
        alert.show()
    }

    private fun saveMoodEntry(emoji: String, noteText: String) {
        val score = EmojiMapper.scoreFor(emoji)
        val item = Mood(
            System.currentTimeMillis(),
            score,
            emoji,
            noteText
        )
        val moods = prefs.getMoods()
        moods.add(item)
        prefs.saveMoods(moods)
        // Refresh with a sorted copy so the latest entry surfaces at the top.
        adapter.update(moods.sortedByDescending { it.timestamp }.toMutableList())
    }
}
