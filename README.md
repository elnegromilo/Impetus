# Impetus

App Android que te envía cada mañana una frase de enfoque y disciplina como notificación, y la muestra también en la pantalla principal.

## Cómo funciona

- **Frase del día:** `QuoteRepository` elige la frase según el día del año, así que es la misma durante todo el día y dos días seguidos nunca repiten.
- **Notificación diaria:** `FocusScheduler` programa con WorkManager un trabajo periódico de 24 h que arranca en la próxima 08:00 local (cambia `NOTIFICATION_HOUR` para otra hora). WorkManager agrupa tareas, así que la hora puede variar unos minutos.
- **Permisos:** en Android 13+ la app pide `POST_NOTIFICATIONS`. Si se rechaza, un aviso con acceso directo a los ajustes permite activarlas después.
- **Botón de prueba:** dispara la notificación al instante para comprobar que todo funciona.

## Requisitos

- Android Studio reciente (el proyecto usa AGP 9.2.1)
- `minSdk` 26 (Android 8.0), `targetSdk` 36

## Ejecutar

```bash
./gradlew installDebug      # instalar en un dispositivo o emulador
./gradlew test              # pruebas unitarias
```

## Estructura

| Archivo | Responsabilidad |
| --- | --- |
| `MainActivity.kt` | Pantalla principal, permisos y botón de prueba |
| `FocusScheduler.kt` | Programación de la tarea diaria (hora fija) |
| `FocusWorker.kt` | Tarea en segundo plano que muestra la notificación |
| `NotificationHelper.kt` | Canal y construcción de la notificación |
| `QuoteRepository.kt` | Lista de frases y selección por fecha |

## Personalizar

- **Frases:** edita la lista en `QuoteRepository.kt`.
- **Hora de envío:** `FocusScheduler.NOTIFICATION_HOUR`.
- **Identificador de la app:** `applicationId` en `app/build.gradle.kts` (el `namespace` de código sigue siendo `com.example.impetus`).
