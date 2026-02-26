# 🏥 AI Medical Symptom Tracker & Health Journal

Android-приложение для ведения медицинского дневника с AI-генерацией отчётов для врача.

## Возможности

- **Симптомы** — тип, интенсивность (0-10), длительность, триггеры, заметки
- **Показатели** — давление, пульс, температура, SpO2, глюкоза, вес, сон, шаги
- **Лекарства** — название, дозировка, расписание, отслеживание приёма
- **AI-отчёт для врача** — Claude генерирует структурированное резюме на русском
- **Анализ паттернов** — AI находит корреляции между симптомами и показателями
- **Экспорт/импорт** — ZIP-архив через SAF (Storage Access Framework)
- **SMS врачу** — отправка отчёта через SMS или буфер обмена

## Технологии

| Слой | Технологии |
|------|-----------|
| UI | Jetpack Compose, Material3 |
| Архитектура | Clean Architecture, MVVM |
| База данных | Room (SQLite, local-first) |
| DI | Hilt |
| Сеть | Retrofit + OkHttp → Claude Messages API |
| Сериализация | kotlinx.serialization |
| Настройки | DataStore |

## Структура проекта

```
com.healthjournal/
├── data/
│   ├── local/
│   │   ├── entity/       # Room entities (7)
│   │   ├── dao/          # DAOs (7)
│   │   ├── converter/    # TypeConverters
│   │   └── migration/    # DB migrations
│   ├── remote/
│   │   ├── api/          # ClaudeApi (Retrofit)
│   │   ├── dto/          # Request/Response DTOs
│   │   ├── interceptor/  # Auth interceptor
│   │   ├── PromptTemplates.kt
│   │   └── ClaudeAiProvider.kt
│   └── repository/       # Repository implementations + Mappers
├── domain/
│   ├── model/            # Domain models
│   ├── repository/       # Repository interfaces
│   └── usecase/          # Use cases
├── presentation/
│   ├── screen/
│   │   ├── onboarding/   # 3-page onboarding + AI consent
│   │   ├── home/         # Dashboard, AddSymptom, AddVital
│   │   ├── vitals/       # Vitals list
│   │   ├── medications/  # Medications list, AddMedication
│   │   └── ai/           # AI Summary + Pattern Analysis
│   ├── navigation/       # NavHost + bottom nav
│   ├── theme/            # Material3 theme
│   └── MainActivity.kt
├── di/                   # Hilt modules
└── util/                 # Export/Import ZIP, SMS helper
```

## Настройка

1. Склонируйте проект
2. Добавьте API-ключ Claude в `gradle.properties`:
   ```
   CLAUDE_API_KEY=sk-ant-api03-...
   ```
3. Синхронизируйте Gradle
4. Запустите на устройстве/эмуляторе (minSdk 26, Android 8.0+)

## Безопасность AI

- AI **НЕ ставит диагнозы** и **НЕ назначает лекарства**
- Используются осторожные формулировки: «возможные причины», «стоит обсудить с врачом»
- Персональные данные (телефоны, email) можно редактировать перед отправкой
- Согласие на использование AI запрашивается при первом запуске
- Все данные хранятся локально на устройстве

## Требования

- Android 8.0+ (API 26)
- Kotlin 2.1.0
- AGP 8.7.3
- Internet — только для AI (по запросу пользователя)
