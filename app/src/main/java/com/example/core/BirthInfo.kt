package com.example.core

import java.time.LocalDate
import java.time.Period
import java.time.Year
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Shared, deterministic birth information used by account settings and the news ticker. */
data class BirthInfo(
    val gregorianAgeYears: Int,
    val gregorianAgeMonths: Int,
    val gregorianAgeDays: Int,
    val hijriAgeYearsApprox: Int,
    val zodiac: String,
    val birthWeekday: String,
    val hijriBirthDateApprox: String,
    val nextBirthday: LocalDate,
    val daysUntilNextBirthday: Long
)

object BirthInfoCalculator {
    /**
     * Parses ISO dates and day-first dates (d/M/yyyy, d-M-yyyy, d.M.yyyy).
     * Arabic-Indic and Eastern Arabic-Indic digits are normalized before parsing.
     */
    fun parseBirthDate(raw: String?): LocalDate? {
        val value = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val asciiDigits = buildString(value.length) {
            value.forEach { char ->
                append(
                    when (char) {
                        in '\\u0660'..'\\u0669' -> ('0'.code + char.code - '\\u0660'.code).toChar()
                        in '\\u06F0'..'\\u06F9' -> ('0'.code + char.code - '\\u06F0'.code).toChar()
                        else -> char
                    }
                )
            }
        }
        val formats = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/uuuu", Locale.ROOT),
            DateTimeFormatter.ofPattern("d-M-uuuu", Locale.ROOT),
            DateTimeFormatter.ofPattern("d.M.uuuu", Locale.ROOT)
        ).map { it.withResolverStyle(ResolverStyle.STRICT) }
        return formats.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(asciiDigits, formatter) }.getOrNull()
        }
    }

    /** Returns null for a missing/future birth date. A February 29 birthday is observed on February 28 in non-leap years. */
    fun compute(
        birthDate: LocalDate?,
        today: LocalDate = LocalDate.now(),
        locale: Locale = Locale("ar")
    ): BirthInfo? {
        val birth = birthDate ?: return null
        if (birth.isAfter(today)) return null

        val birthHijri = HijrahDate.from(birth)
        val todayHijri = HijrahDate.from(today)
        val birthHijriMonth = birthHijri.get(ChronoField.MONTH_OF_YEAR)
        val birthHijriDay = birthHijri.get(ChronoField.DAY_OF_MONTH)
        val todayHijriMonth = todayHijri.get(ChronoField.MONTH_OF_YEAR)
        val todayHijriDay = todayHijri.get(ChronoField.DAY_OF_MONTH)
        val hijriAge = todayHijri.get(ChronoField.YEAR) - birthHijri.get(ChronoField.YEAR) -
            if (todayHijriMonth * 32 + todayHijriDay < birthHijriMonth * 32 + birthHijriDay) 1 else 0

        val age = Period.between(birth, today)
        val thisYearsBirthday = birthdayInYear(birth, today.year)
        val nextBirthday = if (thisYearsBirthday.isBefore(today)) birthdayInYear(birth, today.year + 1) else thisYearsBirthday
        return BirthInfo(
            gregorianAgeYears = age.years,
            gregorianAgeMonths = age.months,
            gregorianAgeDays = age.days,
            hijriAgeYearsApprox = hijriAge,
            zodiac = zodiacFor(birth),
            birthWeekday = birth.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
            hijriBirthDateApprox = "${birthHijriDay}/${birthHijriMonth}/${birthHijri.get(ChronoField.YEAR)} هـ (تقريبي)",
            nextBirthday = nextBirthday,
            daysUntilNextBirthday = ChronoUnit.DAYS.between(today, nextBirthday)
        )
    }

    private fun birthdayInYear(birth: LocalDate, year: Int): LocalDate =
        if (birth.monthValue == 2 && birth.dayOfMonth == 29 && !Year.isLeap(year.toLong())) LocalDate.of(year, 2, 28)
        else birth.withYear(year)

    private fun zodiacFor(date: LocalDate): String = when (date.monthValue * 100 + date.dayOfMonth) {
        in 321..419 -> "الحمل"
        in 420..520 -> "الثور"
        in 521..620 -> "الجوزاء"
        in 621..722 -> "السرطان"
        in 723..822 -> "الأسد"
        in 823..922 -> "العذراء"
        in 923..1022 -> "الميزان"
        in 1023..1121 -> "العقرب"
        in 1122..1221 -> "القوس"
        in 1222..1231, in 101..119 -> "الجدي"
        in 120..218 -> "الدلو"
        else -> "الحوت"
    }
}
