# Impetus

App Android que te acompaña cada día con una **frase de enfoque** y un **versículo de la Biblia**, y te los envía como notificación a la hora que elijas.

## Qué hace

- **Frase y versículo del día:** 371 frases y 454 versículos que rotan por fecha. La misma fecha siempre muestra lo mismo y dos días seguidos nunca se repiten.
- **Fechas especiales:** Año Nuevo, Bajada de Reyes, San Valentín, Día de la Mujer, Semana Santa (calculada cada año), Día del Trabajo, Día de la Madre y del Padre, Fiestas Patrias, Santa Rosa de Lima, Canción Criolla, Todos los Santos, cuenta regresiva a Navidad, Nochebuena, Navidad y Fin de año. Y el 1 de junio, el cumpleaños de Impetus.
- **Recordatorio diario:** eliges la hora y si quieres recibir la frase, el versículo o ambos.
- **Compartir y copiar** el contenido del día.
- **Tema claro y oscuro** según el teléfono.

Los versículos son de la **Reina-Valera 1909** (dominio público), con la ortografía actualizada. Se extrajeron de [scrollmapper/bible_databases](https://github.com/scrollmapper/bible_databases).

## Descargar la APK

Cada cambio en `master` compila la app en GitHub Actions y publica `impetus.apk` en **Releases → Impetus – última versión**.

## Desarrollo

```bash
./gradlew installDebug      # instalar en un dispositivo o emulador
./gradlew test              # pruebas unitarias
```

| Archivo | Responsabilidad |
| --- | --- |
| `MainActivity.kt` | Pantalla principal |
| `SettingsSheet.kt` | Ajustes del recordatorio (hoja inferior) |
| `DailyContent.kt` | Elige la frase y el versículo de cada día |
| `Occasions.kt` | Fechas especiales (fijas y móviles) |
| `QuoteData.kt` / `VerseData.kt` | Las frases y los versículos |
| `FocusScheduler.kt` / `FocusWorker.kt` | Programación y envío de la notificación |
| `NotificationHelper.kt` | Canal y diseño de la notificación |
| `ImpetusPreferences.kt` | Ajustes guardados en el teléfono |

### Personalizar

- **Frases:** edita la lista de `QuoteData.kt` (cuida que no se repitan; hay un test que lo revisa).
- **Fechas especiales:** agrega una entrada en `Occasions.kt`.
- **Firma:** `app/debug.keystore` es una llave de depuración compartida para que la APK de GitHub y la de tu PC se actualicen entre sí. No la uses para publicar en Play Store.
