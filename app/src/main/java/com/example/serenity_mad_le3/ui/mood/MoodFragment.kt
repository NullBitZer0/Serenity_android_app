package com.example.serenity_mad_le3.ui.mood

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serenity_mad_le3.R
import com.example.serenity_mad_le3.data.Prefs
import com.example.serenity_mad_le3.model.Mood
import com.example.serenity_mad_le3.util.EmojiMapper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MoodFragment : Fragment() {
    private lateinit var prefs: Prefs
    private lateinit var adapter: MoodHistoryAdapter
    private lateinit var allMoods: MutableList<Mood>
    private lateinit var selectedDateText: TextView
    private lateinit var clearFilterButton: View
    private var selectedDayStart: Long? = null
    private var selectedDateUtc: Long? = null
    private val dateFormatter by lazy { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    private val emojiChoices: List<String> by lazy { EmojiMapper.allEmojis }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val content = inflater.inflate(R.layout.fragment_mood, container, false)
        return wrapIfLandscape(content)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())

        // Keep the history list reverse-chronological so the latest mood is easy to find.
        allMoods = prefs.getMoods().apply { sortByDescending { it.timestamp } }
        adapter = MoodHistoryAdapter(allMoods.toMutableList())
        val rv = view.findViewById<RecyclerView>(R.id.recyclerMoods)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rv.isNestedScrollingEnabled = false
        } else {
            rv.isNestedScrollingEnabled = true
        }

        val toolbar = view.findViewById<MaterialToolbar>(R.id.moodToolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_share_mood -> {
                    shareMoodSummary()
                    true
                }
                else -> false
            }
        }

        val note = view.findViewById<EditText>(R.id.editNote)
        selectedDateText = view.findViewById(R.id.textSelectedDate)
        clearFilterButton = view.findViewById(R.id.btnClearFilter)

        view.findViewById<View>(R.id.btnPickDate).setOnClickListener {
            showDatePicker()
        }
        clearFilterButton.setOnClickListener {
            selectedDayStart = null
            selectedDateUtc = null
            applyFilter()
        }

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

        applyFilter()
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
        moods.sortByDescending { it.timestamp }
        prefs.saveMoods(moods)
        // Refresh with a sorted copy so the latest entry surfaces at the top.
        allMoods = moods.toMutableList()
        applyFilter()
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.mood_filter_picker_title)
            .setSelection(selectedDateUtc ?: MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            handleDateSelection(selection)
        }
        picker.show(parentFragmentManager, TAG_DATE_PICKER)
    }

    private fun handleDateSelection(selection: Long?) {
        if (selection == null) return
        selectedDateUtc = selection
        selectedDayStart = toLocalStartOfDay(selection)
        applyFilter()
    }

    private fun toLocalStartOfDay(selectionUtc: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = selectionUtc
        val local = Calendar.getInstance()
        local.set(Calendar.YEAR, utc.get(Calendar.YEAR))
        local.set(Calendar.MONTH, utc.get(Calendar.MONTH))
        local.set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH))
        local.set(Calendar.HOUR_OF_DAY, 0)
        local.set(Calendar.MINUTE, 0)
        local.set(Calendar.SECOND, 0)
        local.set(Calendar.MILLISECOND, 0)
        return local.timeInMillis
    }

    private fun applyFilter() {
        val filtered = if (selectedDayStart != null) {
            val start = selectedDayStart!!
            val end = start + DAY_MILLIS
            allMoods.filter { it.timestamp in start until end }
        } else {
            allMoods
        }

        adapter.update(filtered.toMutableList())

        if (selectedDayStart != null) {
            clearFilterButton.visibility = View.VISIBLE
            val dateLabel = dateFormatter.format(Date(selectedDayStart!!))
            selectedDateText.text = if (filtered.isEmpty()) {
                getString(R.string.mood_filter_showing_date_empty, dateLabel)
            } else {
                getString(R.string.mood_filter_showing_date, dateLabel)
            }
        } else {
            clearFilterButton.visibility = View.GONE
            selectedDateText.text = getString(R.string.mood_filter_showing_all)
        }
    }

    private fun shareMoodSummary() {
        val summary = buildMoodSummary()
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title))
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        if (shareIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)))
        }
    }

    private fun buildMoodSummary(): String {
        if (allMoods.isEmpty()) {
            return getString(R.string.share_empty)
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = today.timeInMillis + DAY_MILLIS
        val start = end - (7 * DAY_MILLIS)
        val recent = allMoods.filter { it.timestamp in start until end }
        val periodMoods = if (recent.isNotEmpty()) recent else allMoods
        val prefix = if (recent.isNotEmpty()) {
            getString(R.string.share_prefix)
        } else {
            getString(R.string.share_prefix_all_time)
        }

        val averageScore = periodMoods.map { it.score }.average()
        val formattedAverage = formatScore(averageScore)
        val favoriteEmoji = periodMoods.groupingBy { it.emoji }.eachCount().maxByOrNull { it.value }?.key ?: ""
        val latestWithNote = periodMoods.firstOrNull { !it.note.isNullOrBlank() }
        val noteText = latestWithNote?.note?.trim()

        val totalLine = resources.getQuantityString(R.plurals.share_total_logged, periodMoods.size, periodMoods.size)
        val averageLine = getString(R.string.share_average_label, formattedAverage)
        val highlightLine = if (favoriteEmoji.isNotBlank()) {
            if (!noteText.isNullOrEmpty()) {
                getString(R.string.share_mood_highlight_with_note, favoriteEmoji, noteText)
            } else {
                getString(R.string.share_mood_highlight, favoriteEmoji)
            }
        } else {
            null
        }

        return buildString {
            append(prefix)
            append(' ')
            append(totalLine)
            append(' ')
            append(averageLine)
            highlightLine?.let {
                append(' ')
                append(it)
            }
        }
    }

    private fun formatScore(value: Double): String {
        if (value.isNaN()) return "0.0"
        val rounded = String.format(Locale.getDefault(), "%.1f", value)
        return when {
            value > 0.0 -> "+" + rounded
            value < 0.0 -> rounded
            else -> "0.0"
        }
    }

    private fun wrapIfLandscape(content: View): View {
        if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            return content
        }
        val scroll = NestedScrollView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
        }
        content.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        scroll.addView(content)
        return scroll
    }

    companion object {
        private const val TAG_DATE_PICKER = "mood_date_picker"
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}








