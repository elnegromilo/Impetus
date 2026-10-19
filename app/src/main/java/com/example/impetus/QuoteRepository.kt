package com.example.impetus

import java.time.LocalDate

object QuoteRepository {

    private val quotes: List<String> = listOf(
        "La disciplina es elegir entre lo que quieres ahora y lo que más quieres.",
        "Ordena tu espacio y ordenarás tu mente. Suelta lo que ya no suma.",
        "Enfocarse en todo es no enfocarse en nada. Elimina o ejecuta.",
        "Tu atención es tu activo más escaso. Gástala como si fuera tu último capital.",
        "La incomodidad de hoy es la ventaja competitiva de mañana.",
        "No esperes motivación. Construye sistemas que te obliguen a avanzar.",
        "Cada hora sin dirección es capital quemado que no vuelve.",
        "El ejecutor promedio planea. El fundador extraordinario ya está en el mercado.",
        "La claridad no llega pensando más – llega actuando y corrigiendo más rápido.",
        "Lo que hagas hoy, aunque sea pequeño, es lo que mañana llamarás tu logro."
    )

    /** Cantidad de frases disponibles. */
    val size: Int get() = quotes.size

    /**
     * Devuelve la frase que corresponde a [date]. Es determinista: la misma fecha
     * siempre da la misma frase, y dos días consecutivos del mismo año nunca
     * repiten frase.
     */
    fun getQuoteForDate(date: LocalDate): String = quotes[date.dayOfYear % quotes.size]

    /** Frase del día de hoy (según la zona horaria del dispositivo). */
    fun getQuoteForToday(): String = getQuoteForDate(LocalDate.now())

    /** Todas las frases, solo para pruebas. */
    internal fun allQuotes(): List<String> = quotes
}
