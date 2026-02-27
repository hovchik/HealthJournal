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

## Multi-language support (i18n)

The app supports 4 UI languages: Russian (default), English, Simplified Chinese, and Spanish.

### How to add a new language

1. **Create string resources**: Add a new directory under `app/src/main/res/` with the appropriate qualifier (e.g. `values-fr` for French, `values-de` for German). Copy `values/strings.xml` into it and translate all values.

2. **Update `locales_config.xml`**: Add a `<locale>` entry in `app/src/main/res/xml/locales_config.xml`:
   ```xml
   <locale android:name="fr" />
   ```

3. **Update `LocaleManager.kt`**: Add the new language code to `supportedLanguages` and add a mapping in `getOutputLanguageName()`.

4. **Update Language Settings screen**: Add a new `LanguageOption` entry in `LanguageSettingsScreen.kt` with the language code and string resource for the label. Add corresponding `language_<name>` entries in all existing `strings.xml` files.

5. **Update AI prompt templates**: Add a new language branch in `SummaryPromptTemplates.forLanguage()` and `PatternPromptTemplates.forLanguage()` inside `AiUseCases.kt`.

6. **Plurals**: If the new language has specific plural rules (like Russian), add the appropriate `<item quantity="...">` entries in the new `strings.xml`.

### Architecture notes

- All user-facing strings use Android string resources via `stringResource(R.string.key)` in Compose.
- `@StringRes` annotations enforce compile-time correctness for resource IDs.
- Language preference is persisted in DataStore as `language_mode` (values: `SYSTEM`, `ru`, `en`, `es`, `zh-CN`).
- Locale switching uses `AppCompatDelegate.setApplicationLocales()` which works on Android 13+ natively and pre-13 via AppCompat.
- AI output language follows the selected UI language by passing `outputLanguage` to the prompt builders.
- Export `meta.json` includes `locale` and `uiLanguage` fields; import does NOT auto-switch the user's language.

## Требования

- Android 8.0+ (API 26)
- Kotlin 2.1.0
- AGP 8.7.3
- Internet — только для AI (по запросу пользователя)
