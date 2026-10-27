package com.linguaflow.app.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyProgressStats(
    val streakDays: Int,
    val completedToday: Int,
    val dailyTarget: Int = 3
)

/**
 * Gestor persistente de Racha de Días Reales y Meta Diaria de Estudio por usuario.
 */
object DailyGoalAndStreakManager {

    private const val PREFS_NAME = "linguaflow_daily_streak_prefs"
    private const val DAILY_TARGET = 3

    private fun todayKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun yesterdayKey(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun getStats(context: Context, userId: String): DailyProgressStats {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = todayKey()
        val yesterday = yesterdayKey()

        val lastDate = prefs.getString("${userId}_last_date", null)
        var streak = prefs.getInt("${userId}_streak", 1).coerceAtLeast(1)
        val storedTodayDate = prefs.getString("${userId}_today_date", null)
        val completedToday = if (storedTodayDate == today) {
            prefs.getInt("${userId}_completed_today", 0)
        } else {
            0
        }

        // Si dejó pasar más de 1 día sin entrenar, la racha vuelve a 1
        if (lastDate != null && lastDate != today && lastDate != yesterday) {
            streak = 1
        }

        return DailyProgressStats(
            streakDays = streak,
            completedToday = completedToday,
            dailyTarget = DAILY_TARGET
        )
    }

    fun recordSessionCompleted(context: Context, userId: String): DailyProgressStats {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = todayKey()
        val yesterday = yesterdayKey()

        val lastDate = prefs.getString("${userId}_last_date", null)
        var streak = prefs.getInt("${userId}_streak", 1).coerceAtLeast(1)

        when (lastDate) {
            today -> {
                // Ya entrenó hoy, mantiene la racha del día
            }
            yesterday -> {
                // Entrenó ayer y hoy: suma +1 día real a la racha
                streak += 1
            }
            else -> {
                // Primer entrenamiento o racha reiniciada
                streak = 1
            }
        }

        val storedTodayDate = prefs.getString("${userId}_today_date", null)
        val prevCompleted = if (storedTodayDate == today) {
            prefs.getInt("${userId}_completed_today", 0)
        } else {
            0
        }
        val newCompleted = prevCompleted + 1

        prefs.edit()
            .putString("${userId}_last_date", today)
            .putInt("${userId}_streak", streak)
            .putString("${userId}_today_date", today)
            .putInt("${userId}_completed_today", newCompleted)
            .apply()

        return DailyProgressStats(
            streakDays = streak,
            completedToday = newCompleted,
            dailyTarget = DAILY_TARGET
        )
    }
}
