# Google Play Data Safety — HealthJournal

This document maps HealthJournal's behavior onto the questions asked by the
Google Play Console _Data safety_ form. Values below are the answers to enter
in the form.

## Data collection and sharing

### Health & fitness
None collected through platform APIs. The app does not integrate with Health
Connect, Google Fit, or any wearable SDK. Any symptom, medication, or vital-sign
notes the user types into the journal are stored as free-form "personal notes"
in the app's private sandbox and are never read from, or written to, platform
health data stores.

### Personal info
| Data type | Collected | Shared | Purpose |
|---|---|---|---|
| Name | Yes (user-entered) | Only if user enables Cloud AI and name appears in journal | App functionality |
| Other info (doctor phone number, family-member relationships) | Yes | Only if user enables Cloud AI; phone redaction on by default | App functionality |

### Financial info
None collected. (In-app subscription purchases are processed by Google Play
Billing — Google is the data controller for the transaction.)

### Location
None. No `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` / `ACCESS_BACKGROUND_LOCATION` permissions.

### Web browsing / messages / contacts / photos / audio / files / calendar
None collected.

### App activity, App info and performance, Device or other IDs
None collected. No analytics SDK, no crash-reporting SDK, no ad SDK, no
advertising ID access.

## Security practices

| Practice | Status |
|---|---|
| Data encrypted in transit | **Yes** — all network calls are HTTPS; cleartext is blocked app-wide via `network_security_config.xml`. |
| Users can request data deletion | **Yes** — "Clear all data" in Settings wipes the local DB, preferences, audit log, and cached reports. Since no server holds user data, this is complete deletion. |
| Data is handled per Families Policy | N/A — app is not primarily targeted at children. |
| Independent security review | Not yet. |
| Committed to Play Families Policy | No. |

## Data types shared with third parties — detail

Sharing occurs **only** when the user enables optional Cloud AI analysis and
configures a provider API key. The provider is always one the user chose:

- Anthropic Claude (`api.anthropic.com`)
- OpenAI (`api.openai.com`)
- Google Gemini (`generativelanguage.googleapis.com`)
- DeepSeek (`api.deepseek.com`)

Redaction of phone numbers and email addresses is on by default.

No data is shared with:
- advertising networks (no AdMob / ads SDKs)
- analytics providers (no Firebase Analytics, no GA, no Crashlytics, no Sentry)
- data brokers, insurers, or employers.

## Permissions declared in the manifest

| Permission | Declared | Justification |
|---|---|---|
| `INTERNET` | Yes | Cloud AI analysis (user-enabled) and model download catalog |
| `POST_NOTIFICATIONS` | Yes | Medication reminders and appointment alerts |
| `USE_EXACT_ALARM` / `SCHEDULE_EXACT_ALARM` | Yes | Precise medication-dose reminders (drift from inexact alarms would harm dosing accuracy — qualifies for the "Calendar / reminder app" use case) |
| `USE_BIOMETRIC` | Yes | Optional app lock |

Permissions **intentionally not declared** (previous versions had them;
removed for Play compliance):

- `SEND_SMS` — removed. Share-to-doctor uses `Intent.ACTION_SENDTO` with
  `smsto:` URI, which launches the user's default SMS app with the message
  pre-filled. The user presses "send" themselves, so no `SEND_SMS` permission
  is needed.
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_AUDIO` / `MANAGE_EXTERNAL_STORAGE` —
  removed. AI model import now uses the Storage Access Framework document
  picker, and the "scan for models" feature now only inspects app-owned
  directories (`filesDir`, `getExternalFilesDir()`), which require no
  permission.
- `android.permission.health.*` (Health Connect read permissions) — removed.
  The app no longer integrates with Health Connect; users record vitals
  manually in the journal.

## Backup behavior

- `android:allowBackup="false"`
- `android:dataExtractionRules="@xml/data_extraction_rules"` — excludes all
  domains from cloud backup and device-to-device transfer.
- Users control their data transfer via the in-app ZIP export.

## Account deletion

The app has no remote account system. All data is local to the device. In-app
"Clear all data" satisfies the Play Store account-deletion requirement.
