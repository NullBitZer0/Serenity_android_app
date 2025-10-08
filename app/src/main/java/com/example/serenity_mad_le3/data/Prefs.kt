package com.example.serenity_mad_le3.data

import android.content.Context
import com.example.serenity_mad_le3.model.Habit
import com.example.serenity_mad_le3.model.Mood
import com.example.serenity_mad_le3.model.Settings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class Prefs(context: Context) {
    private val prefs = context.getSharedPreferences("serenity_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getHabits(): MutableList<Habit> {
        val json = prefs.getString(KEY_HABITS, null)
        if (json == null) {
            val seeded = seedHabits()
            saveHabits(seeded)
            return seeded
        }
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        val habits: MutableList<Habit> = gson.fromJson(json, type)
        return habits.apply { 
            resetIfNeeded()
            sanitizeHabits() 
        }
    }

    fun saveHabits(list: List<Habit>) {
        prefs.edit().putString(KEY_HABITS, gson.toJson(list)).apply()
    }

    fun getMoods(): MutableList<Mood> {
        if (!prefs.getBoolean(KEY_MOODS_CLEARED_ONCE, false)) {
            prefs.edit().putBoolean(KEY_MOODS_CLEARED_ONCE, true).apply()
            saveMoods(emptyList())
            return mutableListOf()
        }

        val json = prefs.getString(KEY_MOODS, null)
        if (json == null) {
            saveMoods(emptyList())
            return mutableListOf()
        }
        val type = object : TypeToken<MutableList<Mood>>() {}.type
        val moods: MutableList<Mood> = gson.fromJson(json, type)
        val cleaned = moods.apply { sanitizeMoods() }
        if (cleaned.removeLegacySeedMoods()) {
            saveMoods(cleaned)
        }
        return cleaned
    }

    fun saveMoods(list: List<Mood>) {
        prefs.edit().putString(KEY_MOODS, gson.toJson(list)).apply()
    }

    fun getSettings(): Settings {
        val json = prefs.getString(KEY_SETTINGS, null) ?: return Settings()
        return gson.fromJson(json, Settings::class.java)
    }

    fun saveSettings(settings: Settings) {
        prefs.edit().putString(KEY_SETTINGS, gson.toJson(settings)).apply()
    }

    fun getLastResetDate(): String? = prefs.getString(KEY_LAST_RESET_DATE, null)

    fun setLastResetDate(date: String) { prefs.edit().putString(KEY_LAST_RESET_DATE, date).apply() }
    
    fun getCurrentDate(): String = dateFormat.format(Date())
    
    fun recordHabitCompletion(habitId: String, completed: Boolean) {
        val habits = getHabits()
        val habit = habits.find { it.id == habitId }
        if (habit != null) {
            habit.completionHistory[getCurrentDate()] = completed
            saveHabits(habits)
        }
    }

    companion object {
        private const val KEY_HABITS = "HABITS_LIST"
        private const val KEY_MOODS = "MOOD_LOG"
        private const val KEY_SETTINGS = "SETTINGS"
        private const val KEY_LAST_RESET_DATE = "LAST_RESET_DATE"
        private const val KEY_MOODS_CLEARED_ONCE = "MOODS_CLEARED_ONCE"

        private val LEGACY_MOOD_NOTES = setOf(
            "Morning walk was nice",
            "Great study session",
            "Average day",
            "Felt a bit tired",
            "Stressful commute"
        )
    }

    private fun seedHabits(): MutableList<Habit> = mutableListOf(
        Habit(
            id = UUID.randomUUID().toString(),
            title = "Drink Water",
            description = "Stay hydrated throughout the day",
            emoji = Habit.DEFAULT_EMOJI,
            currentCount = 5,
            targetCount = 8
        ),
        Habit(
            id = UUID.randomUUID().toString(),
            title = "Exercise",
            description = "30 minutes of physical activity",
            emoji = "\uD83C\uDFC3",
            currentCount = 1,
            targetCount = 1,
            completedToday = true
        ),
        Habit(
            id = UUID.randomUUID().toString(),
            title = "Read",
            description = "Read for personal growth",
            emoji = "\uD83D\uDCDA",
            currentCount = 15,
            targetCount = 30
        ),
        Habit(
            id = UUID.randomUUID().toString(),
            title = "Meditate",
            description = "Practice mindfulness",
            emoji = "\uD83D\uDE4F",
            currentCount = 5,
            targetCount = 10
        )
    )

    private fun MutableList<Habit>.resetIfNeeded() {
        val today = getCurrentDate()
        val lastReset = getLastResetDate()
        
        // If we haven't reset today, reset all habits
        if (lastReset != today) {
            forEach { habit ->
                // Record the previous day's completion status before resetting
                if (lastReset != null) {
                    habit.completionHistory[lastReset] = habit.completedToday
                }
                
                // Reset the habit for today
                habit.currentCount = 0
                habit.completedToday = false
            }
            
            // Update the last reset date
            setLastResetDate(today)
            saveHabits(this)
        }
    }

    private fun MutableList<Habit>.sanitizeHabits() {
        forEach { habit ->
            if (habit.emoji.isNullOrBlank()) habit.emoji = Habit.DEFAULT_EMOJI
            habit.description = habit.description?.trim() ?: ""
            if (habit.targetCount <= 0) habit.targetCount = 1
            habit.currentCount = habit.currentCount.coerceIn(0, habit.targetCount)
            habit.completedToday = habit.currentCount >= habit.targetCount
            
            // Initialize completionHistory if it's null (for backward compatibility)
            if (habit.completionHistory == null) {
                habit.completionHistory = mutableMapOf()
            }
        }
    }

    private fun MutableList<Mood>.sanitizeMoods() {
        sortByDescending { it.timestamp }
    }

    private fun MutableList<Mood>.removeLegacySeedMoods(): Boolean {
        val initialSize = size
        removeAll { mood ->
            mood.note != null && mood.note in LEGACY_MOOD_NOTES
        }
        return size != initialSize
    }
}