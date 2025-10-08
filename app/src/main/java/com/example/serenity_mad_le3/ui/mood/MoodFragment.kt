package com.example.serenity_mad_le3.ui.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        val list = prefs.getMoods().apply { sortByDescending { it.timestamp } }
        adapter = MoodHistoryAdapter(list)
        val rv = view.findViewById<RecyclerView>(R.id.recyclerMoods)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val note = view.findViewById<EditText>(R.id.editNote)

        view.findViewById<Button>(R.id.btnSaveMood).setOnClickListener {
            val noteText = note.text.toString().trim()
            if (noteText.isEmpty()) {
                note.error = getString(R.string.mood_note_required)
                note.requestFocus()
            } else {
                showEmojiPicker(note, noteText)
            }
        }

        view.findViewById<Button>(R.id.btnViewChart).setOnClickListener {
            findNavController().navigate(R.id.action_mood_to_chart)
        }
    }

    private fun showEmojiPicker(noteField: EditText, noteText: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_emoji_grid, null)
        val grid = dialogView.findViewById<GridLayout>(R.id.layoutEmojiGrid)

        var alert: AlertDialog? = null
        emojiChoices.forEach { emoji ->
            val button = LayoutInflater.from(requireContext()).inflate(R.layout.item_emoji_option, grid, false) as Button
            button.text = emoji
            button.contentDescription = getString(R.string.emoji_option_description, emoji)
            button.setOnClickListener {
                saveMoodEntry(emoji, noteText)
                noteField.text.clear()
                noteField.error = null
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
        adapter.update(moods.sortedByDescending { it.timestamp }.toMutableList())
    }
}
