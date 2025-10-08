package com.example.serenity_mad_le3.util

import java.time.LocalDate
import java.time.ZoneId

object DateUtils {
    fun todayString(): String = LocalDate.now(ZoneId.systemDefault()).toString()
}

