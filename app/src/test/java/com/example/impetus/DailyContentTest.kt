package com.example.impetus

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyContentTest {

    @Test
    fun hasAtLeastOneQuoteAndVersePerDayOfTheYear() {
        assertTrue(DailyContentRepository.quoteCount >= 365)
        assertTrue(DailyContentRepository.verseCount >= 365)
    }

    @Test
    fun quotesAndVersesAreUniqueAndNotBlank() {
        val quotes = QuoteData.pool
        assertTrue(quotes.all { it.isNotBlank() })
        assertEquals(quotes.size, quotes.toSet().size)

        val verses = VerseData.pool
        assertTrue(verses.all { it.text.isNotBlank() && it.reference.isNotBlank() })
        assertEquals(verses.size, verses.map { it.reference }.toSet().size)
    }

    @Test
    fun keepsTheOriginalHelloWorldQuote() {
        assertTrue("TU LOGRASTE ESTO GAAA" in QuoteData.pool)
    }

    @Test
    fun sameDateGivesSameContent() {
        val date = LocalDate.of(2026, 10, 19)
        assertEquals(DailyContentRepository.forDate(date), DailyContentRepository.forDate(date))
    }

    @Test
    fun consecutiveDaysNeverRepeat() {
        var day = LocalDate.of(2026, 1, 1)
        repeat(3 * 366) {
            val next = day.plusDays(1)
            val a = DailyContentRepository.forDate(day)
            val b = DailyContentRepository.forDate(next)
            assertNotEquals("Frase repetida entre $day y $next", a.quote, b.quote)
            // Fiestas Patrias (28 y 29 de julio) comparten versículo a propósito.
            if (!(a.occasion != null && a.occasion?.verse == b.occasion?.verse)) {
                assertNotEquals("Versículo repetido entre $day y $next", a.verse, b.verse)
            }
            day = next
        }
    }

    @Test
    fun christmasHasItsOwnContent() {
        val content = DailyContentRepository.forDate(LocalDate.of(2026, 12, 25))
        assertNotNull(content.occasion)
        assertEquals("Lucas 2:11", content.verse.reference)
    }

    @Test
    fun christmasCountdownShowsDaysLeft() {
        val occasion = Occasions.forDate(LocalDate.of(2026, 12, 20))
        assertEquals("🎄 Faltan 5 días para Navidad", occasion?.label)
    }

    @Test
    fun impetusBirthdayShowsHelloWorld() {
        assertEquals("TU LOGRASTE ESTO GAAA", Occasions.forDate(LocalDate.of(2027, 6, 1))?.quote)
    }

    @Test
    fun easterDatesAreCorrect() {
        assertEquals(LocalDate.of(2025, 4, 20), Occasions.easterSunday(2025))
        assertEquals(LocalDate.of(2026, 4, 5), Occasions.easterSunday(2026))
        assertEquals(LocalDate.of(2027, 3, 28), Occasions.easterSunday(2027))
        assertEquals("Mateo 28:6", Occasions.forDate(LocalDate.of(2026, 4, 5))?.verse?.reference)
        assertEquals("Isaías 53:5", Occasions.forDate(LocalDate.of(2026, 4, 3))?.verse?.reference)
    }

    @Test
    fun parentsDaysFollowPeruvianCalendar() {
        // Segundo domingo de mayo y tercer domingo de junio.
        assertEquals("Proverbios 31:28", Occasions.forDate(LocalDate.of(2026, 5, 10))?.verse?.reference)
        assertEquals("Proverbios 4:1", Occasions.forDate(LocalDate.of(2026, 6, 21))?.verse?.reference)
    }

    @Test
    fun regularDayHasNoOccasion() {
        assertNull(Occasions.forDate(LocalDate.of(2026, 10, 19)))
    }
}
