package com.example.impetus

object QuoteRepository {

    private val quotes: List<String> = listOf(
        "La disciplina es elegir entre lo que quieres ahora y lo que más quieres.",
        "Si organizar tu espacio puedes, dejar el pasado debes",
        "Enfocarse en todo es no enfocarse en nada. Elimina o ejecuta.",
        "Tu atención es tu activo más escaso. Gástala como si fuera tu último capital.",
        "La incomodidad de hoy es la ventaja competitiva de mañana.",
        "No esperes motivación. Construye sistemas que te obliguen a avanzar.",
        "Cada hora sin dirección es capital quemado que no vuelve.",
        "El ejecutor promedio planea. El fundador extraordinario ya está en el mercado.",
        "La claridad no llega pensando más – llega actuando y corrigiendo más rápido.",
        "TU LOGRASTE ESTO GAAA"
    )

    /**
     * Devuelve una frase aleatoria de la colección.
     */
    fun getRandomQuote(): String = quotes.random()

    /**
     * Devuelve una frase de forma secuencial basándose en el día del año,
     * garantizando variedad sin repetición inmediata.
     */
    fun getQuoteForToday(): String {
        val dayOfYear = java.util.Calendar.getInstance()
            .get(java.util.Calendar.DAY_OF_YEAR)
        return quotes[dayOfYear % quotes.size]
    }
}