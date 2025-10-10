package com.example.serenity_mad_le3.model

data class Settings(
    var hydrationIntervalMinutes: Int = 120,
    var notificationsEnabled: Boolean = true,
    var darkModeEnabled: Boolean = false
)
