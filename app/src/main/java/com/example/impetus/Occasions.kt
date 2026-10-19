package com.example.impetus

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.MonthDay
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Fechas especiales del año (pensadas para Perú). Las fijas van por día y mes;
 * las móviles (Semana Santa, Día de la Madre, Día del Padre) se calculan cada año.
 */
object Occasions {

    private val fixed: Map<MonthDay, Occasion> = mapOf(
        MonthDay.of(1, 1) to Occasion(
            "🎆 Año Nuevo",
            "Año nuevo, página en blanco. Escríbela con intención desde el primer día.",
            SpecialVerses.ANIO_NUEVO
        ),
        MonthDay.of(1, 6) to Occasion(
            "⭐ Bajada de Reyes",
            "Como los sabios de oriente, sigue tu estrella aunque el camino sea largo.",
            SpecialVerses.REYES
        ),
        MonthDay.of(2, 14) to Occasion(
            "💝 Día del Amor y la Amistad",
            "Hoy celebra a quienes hacen tu camino más ligero. El cariño también se demuestra con tiempo.",
            SpecialVerses.AMISTAD
        ),
        MonthDay.of(3, 8) to Occasion(
            "💜 Día de la Mujer",
            "Hoy celebramos a las mujeres que abren caminos para que otras lleguen más lejos.",
            SpecialVerses.MUJER
        ),
        MonthDay.of(5, 1) to Occasion(
            "🛠️ Día del Trabajo",
            "Hoy se honra el trabajo bien hecho, también el tuyo. Descansa con orgullo.",
            SpecialVerses.TRABAJO
        ),
        MonthDay.of(6, 1) to Occasion(
            "🎂 Cumpleaños de Impetus",
            "TU LOGRASTE ESTO GAAA",
            SpecialVerses.IMPETUS
        ),
        MonthDay.of(7, 28) to Occasion(
            "🇵🇪 Fiestas Patrias",
            "¡Felices Fiestas Patrias! Que el orgullo por nuestra tierra se note en lo que construimos cada día.",
            SpecialVerses.PATRIAS
        ),
        MonthDay.of(7, 29) to Occasion(
            "🇵🇪 Fiestas Patrias",
            "Un país grande se construye con personas que cumplen su parte todos los días.",
            SpecialVerses.PATRIAS
        ),
        MonthDay.of(8, 30) to Occasion(
            "🌹 Santa Rosa de Lima",
            "La grandeza también vive en la humildad y en el servicio a los demás.",
            SpecialVerses.SANTA_ROSA
        ),
        MonthDay.of(10, 31) to Occasion(
            "🎶 Día de la Canción Criolla",
            "Que tu trabajo de hoy tenga ritmo, alegría y mucho corazón.",
            SpecialVerses.CRIOLLA
        ),
        MonthDay.of(11, 1) to Occasion(
            "🕯️ Día de Todos los Santos",
            "Honra a quienes te inspiraron viviendo con el mismo propósito.",
            SpecialVerses.SANTOS
        ),
        MonthDay.of(12, 8) to Occasion(
            "✨ Inmaculada Concepción",
            "Buen día para empezar a preparar el corazón para la Navidad.",
            SpecialVerses.INMACULADA
        ),
        MonthDay.of(12, 18) to christmasCountdown(
            "La Navidad se acerca: cierra el año con gratitud y abre espacio para lo nuevo.",
            SpecialVerses.NAV_18
        ),
        MonthDay.of(12, 19) to christmasCountdown(
            "Haz una lista de lo que lograste este año. Te vas a sorprender.",
            SpecialVerses.NAV_19
        ),
        MonthDay.of(12, 20) to christmasCountdown(
            "El mejor regalo que puedes dar esta Navidad es tu tiempo.",
            SpecialVerses.NAV_20
        ),
        MonthDay.of(12, 21) to christmasCountdown(
            "Que las luces de diciembre te recuerden que siempre hay algo que celebrar.",
            SpecialVerses.NAV_21
        ),
        MonthDay.of(12, 22) to christmasCountdown(
            "Reconcíliate, llama, abraza: la Navidad es para estar cerca.",
            SpecialVerses.NAV_22
        ),
        MonthDay.of(12, 23) to christmasCountdown(
            "Mañana es Nochebuena: prepara la mesa, pero sobre todo el corazón.",
            SpecialVerses.NAV_23
        ),
        MonthDay.of(12, 24) to Occasion(
            "🌟 Nochebuena",
            "Esta noche ningún pendiente importa más que las personas que tienes al lado.",
            SpecialVerses.NOCHEBUENA
        ),
        MonthDay.of(12, 25) to Occasion(
            "🎄 ¡Feliz Navidad!",
            "Que la paz y la esperanza de hoy te acompañen todo el año.",
            SpecialVerses.NAVIDAD
        ),
        MonthDay.of(12, 31) to Occasion(
            "🥂 Fin de año",
            "Despide el año con gratitud: cada día, bueno o difícil, te trajo hasta aquí.",
            SpecialVerses.FIN_ANIO
        )
    )

    fun forDate(date: LocalDate): Occasion? = movable(date) ?: fixed[MonthDay.from(date)]?.let {
        if (date.month == Month.DECEMBER && date.dayOfMonth in 18..23) {
            val days = ChronoUnit.DAYS.between(date, LocalDate.of(date.year, 12, 25))
            it.copy(label = "🎄 Faltan $days días para Navidad")
        } else {
            it
        }
    }

    private fun movable(date: LocalDate): Occasion? {
        val easter = easterSunday(date.year)
        return when (date) {
            easter.minusDays(7) -> Occasion(
                "🌿 Domingo de Ramos",
                "Recibe cada nueva etapa con humildad y con el corazón abierto.",
                SpecialVerses.RAMOS
            )
            easter.minusDays(3) -> Occasion(
                "🍞 Jueves Santo",
                "Servir a otros es una grandeza que no necesita aplausos.",
                SpecialVerses.JUEVES_SANTO
            )
            easter.minusDays(2) -> Occasion(
                "✝️ Viernes Santo",
                "Hoy es día de silencio y reflexión. El amor más grande se muestra con entrega.",
                SpecialVerses.VIERNES_SANTO
            )
            easter -> Occasion(
                "🕊️ Domingo de Resurrección",
                "Toda caída puede tener un nuevo comienzo. Hoy se celebra la esperanza.",
                SpecialVerses.RESURRECCION
            )
            nthSunday(date.year, Month.MAY, 2) -> Occasion(
                "🌷 Día de la Madre",
                "Gracias, mamá: tu esfuerzo silencioso es la base de muchos logros.",
                SpecialVerses.MADRE
            )
            nthSunday(date.year, Month.JUNE, 3) -> Occasion(
                "👔 Día del Padre",
                "Gracias, papá, por enseñar con el ejemplo más que con palabras.",
                SpecialVerses.PADRE
            )
            else -> null
        }
    }

    private fun christmasCountdown(quote: String, verse: Verse) =
        Occasion("🎄 Cuenta regresiva a Navidad", quote, verse)

    private fun nthSunday(year: Int, month: Month, n: Int): LocalDate =
        LocalDate.of(year, month, 1)
            .with(TemporalAdjusters.firstInMonth(DayOfWeek.SUNDAY))
            .plusWeeks((n - 1).toLong())

    /** Domingo de Pascua (calendario gregoriano, algoritmo de Meeus/Jones/Butcher). */
    internal fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }
}
