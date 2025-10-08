package com.example.serenity_mad_le3.model

data class Mood(
    val timestamp: Long,
    val score: Int,
    val emoji: String,
    val note: String? = null
)

