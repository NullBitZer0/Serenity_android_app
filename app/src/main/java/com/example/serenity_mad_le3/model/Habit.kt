package com.example.serenity_mad_le3.model

data class Habit(
    val id: String,
    var title: String,
    var description: String? = "",
    var emoji: String? = DEFAULT_EMOJI,
    var currentCount: Int = 0,
    var targetCount: Int = 1,
    var completedToday: Boolean = false,
    var lastResetDate: String? = null,
    var completionHistory: MutableMap<String, Boolean> = mutableMapOf()
) {
    companion object {
        const val DEFAULT_EMOJI = "\uD83D\uDCAA"
    }
}