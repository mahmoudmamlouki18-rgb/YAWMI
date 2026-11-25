package com.example.core.time

import com.example.core.preferences.AppLanguage
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class GreetingPeriod {
  MORNING,
  AFTERNOON,
  EVENING,
}

interface LocalClock {
  fun currentDate(): LocalDate

  fun currentTime(): LocalTime

  fun currentEpochMs(): Long

  fun greetingPeriod(): GreetingPeriod
}

class SystemLocalClock : LocalClock {
  override fun currentDate(): LocalDate = LocalDate.now(ZoneId.systemDefault())

  override fun currentTime(): LocalTime = LocalTime.now(ZoneId.systemDefault())

  override fun currentEpochMs(): Long = System.currentTimeMillis()

  override fun greetingPeriod(): GreetingPeriod {
    val hour = currentTime().hour
    return when {
      hour < 12 -> GreetingPeriod.MORNING
      hour < 17 -> GreetingPeriod.AFTERNOON
      else -> GreetingPeriod.EVENING
    }
  }
}

fun LocalDate.formatLocalizedHeader(language: AppLanguage): String {
  val locale = Locale.forLanguageTag(language.tag)
  val pattern =
    if (language == AppLanguage.ARABIC) "EEEE، d MMMM yyyy" else "EEEE, MMM d, yyyy"
  return format(DateTimeFormatter.ofPattern(pattern, locale))
}
