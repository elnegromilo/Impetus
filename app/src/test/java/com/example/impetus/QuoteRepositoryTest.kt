package com.example.impetus

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteRepositoryTest {

    @Test
    fun hasAtLeastTwoQuotes() {
        assertTrue(QuoteRepository.size >= 2)
    }

    @Test
    fun quotesAreNotBlankAndNotRepeated() {
        val quotes = QuoteRepository.allQuotes()
        assertTrue(quotes.all { it.isNotBlank() })
        assertEquals(quotes.size, quotes.toSet().size)
    }

    @Test
    fun sameDateGivesSameQuote() {
        val date = LocalDate.of(2026, 10, 19)
        assertEquals(QuoteRepository.getQuoteForDate(date), QuoteRepository.getQuoteForDate(date))
    }

    @Test
    fun consecutiveDaysInAYearNeverRepeat() {
        var day = LocalDate.of(2026, 1, 1)
        val lastDay = LocalDate.of(2026, 12, 31)
        while (day.isBefore(lastDay)) {
            val next = day.plusDays(1)
            assertNotEquals(
                "Frase repetida entre $day y $next",
                QuoteRepository.getQuoteForDate(day),
                QuoteRepository.getQuoteForDate(next)
            )
            day = next
        }
    }

    @Test
    fun everyDayOfALeapYearHasAQuote() {
        var day = LocalDate.of(2028, 1, 1)
        while (day.year == 2028) {
            assertTrue(QuoteRepository.getQuoteForDate(day).isNotBlank())
            day = day.plusDays(1)
        }
    }
}
