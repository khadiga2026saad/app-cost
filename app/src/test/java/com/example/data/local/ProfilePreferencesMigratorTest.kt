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
    @Test fun invalidDateIsNull() {
        assertNull(ProfilePreferencesMigrator.normalizeBirthDate("31/02/2000"))
        assertNull(ProfilePreferencesMigrator.normalizeBirthDate("2000-13-01"))
        assertNull(ProfilePreferencesMigrator.normalizeBirthDate("yesterday"))
    }
}
