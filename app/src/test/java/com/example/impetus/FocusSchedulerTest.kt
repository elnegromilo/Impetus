package com.example.impetus

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class FocusSchedulerTest {

    private val lima = ZoneId.of("America/Lima")

    @Test
    fun beforeTheHour_waitsUntilToday() {
        val now = ZonedDateTime.of(2026, 10, 19, 6, 30, 0, 0, lima)
        assertEquals(Duration.ofMinutes(90), FocusScheduler.delayUntilNext(now, 8))
    }

    @Test
    fun afterTheHour_waitsUntilTomorrow() {
        val now = ZonedDateTime.of(2026, 10, 19, 9, 0, 0, 0, lima)
        assertEquals(Duration.ofHours(23), FocusScheduler.delayUntilNext(now, 8))
    }

    @Test
    fun exactlyAtTheHour_waitsAFullDay() {
        val now = ZonedDateTime.of(2026, 10, 19, 8, 0, 0, 0, lima)
        assertEquals(Duration.ofHours(24), FocusScheduler.delayUntilNext(now, 8))
    }

    @Test
    fun respectsDaylightSavingChange() {
        // En Nueva York el 8 de marzo de 2026 se adelanta el reloj una hora,
        // así que de 08:30 a las 08:00 del día siguiente pasan 22,5 h reales.
        val ny = ZoneId.of("America/New_York")
        val now = ZonedDateTime.of(2026, 3, 7, 8, 30, 0, 0, ny)
        assertEquals(Duration.ofMinutes(22 * 60 + 30), FocusScheduler.delayUntilNext(now, 8))
    }
}
