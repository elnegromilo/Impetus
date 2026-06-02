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
     * Ahora esta función también devuelve una frase aleatoria para garantizar
     * variedad durante las pruebas y el uso diario.
     */
    fun getQuoteForToday(): String = getRandomQuote()
}