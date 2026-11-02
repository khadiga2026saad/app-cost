package com.example.core

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class BirthInfoCalculatorTest {
    @Test fun calculatesOrdinaryAgeAndBirthday() {
        val info = BirthInfoCalculator.compute(LocalDate.of(1990, 5, 10), LocalDate.of(2024, 5, 9))!!
        assertEquals(33, info.gregorianAgeYears)
        assertEquals(LocalDate.of(2024, 5, 10), info.nextBirthday)
        assertEquals(1L, info.daysUntilNextBirthday)
        assertEquals("الثور", info.zodiac)
    }

    @Test fun birthdayTodayHasZeroDaysRemaining() {
        val info = BirthInfoCalculator.compute(LocalDate.of(2000, 7, 15), LocalDate.of(2024, 7, 15))!!
        assertEquals(24, info.gregorianAgeYears)
        assertEquals(LocalDate.of(2024, 7, 15), info.nextBirthday)
        assertEquals(0L, info.daysUntilNextBirthday)
    }

    @Test fun februaryTwentyNinthUsesFebruaryTwentyEighthInNonLeapYears() {
        val info = BirthInfoCalculator.compute(LocalDate.of(2000, 2, 29), LocalDate.of(2023, 2, 27))!!
        assertEquals(LocalDate.of(2023, 2, 28), info.nextBirthday)
        assertEquals(1L, info.daysUntilNextBirthday)
        val onBirthday = BirthInfoCalculator.compute(LocalDate.of(2000, 2, 29), LocalDate.of(2024, 2, 29))!!
        assertEquals(LocalDate.of(2024, 2, 29), onBirthday.nextBirthday)
    }

    @Test fun futureBirthDateReturnsNull() {
        assertNull(BirthInfoCalculator.compute(LocalDate.of(2025, 1, 1), LocalDate.of(2024, 12, 31)))
        assertNull(BirthInfoCalculator.compute(null, LocalDate.of(2024, 12, 31)))
    }

    @Test fun zodiacBoundariesAreInclusive() {
        assertEquals("الحوت", BirthInfoCalculator.compute(LocalDate.of(2000, 3, 20), LocalDate.of(2024, 3, 1))!!.zodiac)
        assertEquals("الحمل", BirthInfoCalculator.compute(LocalDate.of(2000, 3, 21), LocalDate.of(2024, 3, 1))!!.zodiac)
        assertEquals("الحمل", BirthInfoCalculator.compute(LocalDate.of(2000, 4, 19), LocalDate.of(2024, 3, 1))!!.zodiac)
        assertEquals("الثور", BirthInfoCalculator.compute(LocalDate.of(2000, 4, 20), LocalDate.of(2024, 3, 1))!!.zodiac)
    }
}
