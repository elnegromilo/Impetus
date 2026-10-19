package com.example.impetus

import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class FocusSchedulerTest {

    private val lima = ZoneId.of("America/Lima")
    private val eight = LocalTime.of(8, 0)

    @Test
    fun beforeTheHour_waitsUntilToday() {
        val now = ZonedDateTime.of(2026, 10, 19, 6, 30, 0, 0, lima)
        assertEquals(Duration.ofMinutes(90), FocusScheduler.delayUntilNext(now, eight))
    }

    @Test
    fun afterTheHour_waitsUntilTomorrow() {
        val now = ZonedDateTime.of(2026, 10, 19, 9, 0, 0, 0, lima)
        assertEquals(Duration.ofHours(23), FocusScheduler.delayUntilNext(now, eight))
    }

    @Test
    fun exactlyAtTheHour_waitsAFullDay() {
        val now = ZonedDateTime.of(2026, 10, 19, 8, 0, 0, 0, lima)
        assertEquals(Duration.ofHours(24), FocusScheduler.delayUntilNext(now, eight))
    }

    @Test
    fun respectsMinutes() {
        val now = ZonedDateTime.of(2026, 10, 19, 7, 0, 0, 0, lima)
        assertEquals(Duration.ofMinutes(90), FocusScheduler.delayUntilNext(now, LocalTime.of(8, 30)))
    }

    @Test
    fun respectsDaylightSavingChange() {
        // En Nueva York el 8 de marzo de 2026 se adelanta el reloj una hora.
        val ny = ZoneId.of("America/New_York")
        val now = ZonedDateTime.of(2026, 3, 7, 8, 30, 0, 0, ny)
        assertEquals(Duration.ofMinutes(22 * 60 + 30), FocusScheduler.delayUntilNext(now, eight))
    }

    @Test
    fun nextReminder_isTomorrowWhenTimePassed() {
        val now = ZonedDateTime.of(2026, 10, 19, 21, 0, 0, 0, lima)
        val next = FocusScheduler.nextReminder(now, eight)
        assertEquals(now.toLocalDate().plusDays(1), next.toLocalDate())
        assertEquals(eight, next.toLocalTime())
    }
}
