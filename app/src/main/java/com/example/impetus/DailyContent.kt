package com.example.impetus

import java.time.LocalDate

/** Un versículo con su cita (por ejemplo, "Juan 3:16"). */
data class Verse(val text: String, val reference: String)

/** Una fecha especial: muestra su propia frase, su versículo y una etiqueta. */
data class Occasion(val label: String, val quote: String, val verse: Verse)

/** Lo que la app muestra en un día concreto. */
data class DailyContent(
    val date: LocalDate,
    val quote: String,
    val verse: Verse,
    val occasion: Occasion?
)

/**
 * Elige la frase y el versículo de cada día.
 *
 * - En fechas especiales (Navidad, Año Nuevo, Semana Santa...) se usa el contenido de la ocasión.
 * - El resto de días se rota por número de día, así que la misma fecha siempre da lo mismo
 *   y dos días seguidos nunca repiten.
 */
object DailyContentRepository {

    val quoteCount: Int get() = QuoteData.pool.size
    val verseCount: Int get() = VerseData.pool.size

    fun forDate(date: LocalDate): DailyContent {
        val occasion = Occasions.forDate(date)
        val day = date.toEpochDay()
        return DailyContent(
            date = date,
            quote = occasion?.quote ?: QuoteData.pool[Math.floorMod(day, quoteCount.toLong()).toInt()],
            verse = occasion?.verse ?: VerseData.pool[Math.floorMod(day, verseCount.toLong()).toInt()],
            occasion = occasion
        )
    }

    fun forToday(): DailyContent = forDate(LocalDate.now())
}
