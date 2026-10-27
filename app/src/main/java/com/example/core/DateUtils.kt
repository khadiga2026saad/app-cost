package com.example.core

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val zoneId: ZoneId = ZoneId.systemDefault()

    val ARABIC_MONTHS = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    val arabicMonthNames: List<String> get() = ARABIC_MONTHS

    fun getMonthName(monthNumber: Int): String {
        return if (monthNumber in 1..12) ARABIC_MONTHS[monthNumber - 1] else ""
    }

    fun currentYear(): Int = LocalDate.now(zoneId).year

    fun currentMonth(): Int = LocalDate.now(zoneId).monthValue

    fun currentEpochMillis(): Long = System.currentTimeMillis()

    fun getMonthStartEpoch(year: Int, month: Int): Long {
        val startOfMonth = LocalDate.of(year, month, 1)
        return startOfMonth.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun getMonthEndEpoch(year: Int, month: Int): Long {
        val ym = YearMonth.of(year, month)
        val endOfMonth = ym.atEndOfMonth()
        return endOfMonth.atTime(23, 59, 59, 999_999_999).atZone(zoneId).toInstant().toEpochMilli()
    }

    fun formatToLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
    }

    fun formatDateArabic(epochMillis: Long): String {
        val localDate = formatToLocalDate(epochMillis)
        val monthName = getMonthName(localDate.monthValue)
        return "${localDate.dayOfMonth} $monthName ${localDate.year}"
    }

    fun formatDateShort(epochMillis: Long): String {
        val localDate = formatToLocalDate(epochMillis)
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.US)
        return localDate.format(formatter)
    }

    fun formatDate(epochMillis: Long): String = formatDateShort(epochMillis)

    fun formatYearMonthArabic(year: Int, month: Int): String {
        val monthName = getMonthName(month)
        return "$monthName $year"
    }
}
