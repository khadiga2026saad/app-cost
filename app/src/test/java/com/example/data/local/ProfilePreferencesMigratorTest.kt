package com.example.data.local

import org.junit.Assert.*
import org.junit.Test

class ProfilePreferencesMigratorTest {
    @Test fun emptyDateIsNull() {
        assertNull(ProfilePreferencesMigrator.normalizeBirthDate(null))
        assertNull(ProfilePreferencesMigrator.normalizeBirthDate(""))
        assertNull(ProfilePreferencesMigrator.normalizeBirthDate("  "))
    }
    @Test fun existingIsoAndLegacyDateNormalize() {
        assertEquals("2000-02-29", ProfilePreferencesMigrator.normalizeBirthDate("2000-02-29"))
        assertEquals("2000-02-29", ProfilePreferencesMigrator.normalizeBirthDate("29/02/2000"))
    }
    @Test fun dayFirstHyphenDatesNormalizeWithOrWithoutLeadingZero() {
        assertEquals("1984-02-11", ProfilePreferencesMigrator.normalizeBirthDate("11-2-1984"))
        assertEquals("1984-02-05", ProfilePreferencesMigrator.normalizeBirthDate("05-02-1984"))
    }
    @Test fun arabicIndicAndPersianDigitsNormalize() {
        assertEquals("1984-02-11", ProfilePreferencesMigrator.normalizeBirthDate("١١-٢-١٩٨٤"))
        assertEquals("1984-02-05", ProfilePreferencesMigrator.normalizeBirthDate("۰۵-۰۲-۱۹۸۴"))
    }
    @Test fun invalidDateIsNull() {
        assertNull(ProfilePreferencesMigrator.normalizeBirthDate("31/02/2000"))
        assertNull(ProfilePreferencesMigrator.normalizeBirthDate("2000-13-01"))
        assertNull(ProfilePreferencesMigrator.normalizeBirthDate("yesterday"))
    }
}
