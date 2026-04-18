# Privacy Policy — HealthJournal

_Last updated: 2026-04-18_

HealthJournal ("the app", "we") is a local-first health journal that helps
individuals and families track symptoms, vital signs, medications, and
appointments. This policy explains exactly what data the app collects, how it
is stored, when it leaves the device, and the controls you have.

## 1. Data we collect

### 1.1 Health and fitness data you enter
- Symptoms (type, intensity 0–10, duration, triggers, notes)
- Vital signs (blood pressure, heart rate, body temperature, SpO₂, blood
  glucose, weight, sleep, steps)
- Medication records (name, dosage, schedule, intake logs)
- Known diseases and medical history notes
- Appointment records and doctor notes

### 1.2 Personal information you enter
- Profile name, age, sex, height, weight
- Family-member profile names and relationships
- Optional doctor name and phone number (for share/report features)

### 1.3 Wearable data (only if you connect Health Connect)
With your explicit Health Connect permission, the app reads:
heart rate, steps, sleep, oxygen saturation, blood pressure, body temperature,
blood glucose, weight. Data is read-only and copied into your local journal.

### 1.4 Device information
We compute RAM, available storage, Android version, and supported ABIs locally
to decide which on-device AI models your device can run. This information is
never transmitted off-device.

### 1.5 App logs
An in-app audit log records data access and modification events so you can
review them. The audit log is stored locally and is wiped when you clear data.

We do **not** collect analytics, advertising identifiers, location, contacts,
photos, microphone input, or crash reports.

## 2. Where data is stored

All health and personal data is stored **locally on your device** inside the
app-private storage area, in a Room/SQLite database and preference files that
other apps cannot read. The database is protected by Android app-sandbox
isolation and, optionally, your biometric / PIN lock.

We do not operate servers that hold your health records. We do not have a user
account system.

Automatic Android cloud backup and device-to-device transfer are **disabled**
for HealthJournal data (`allowBackup="false"`, plus explicit
`data_extraction_rules.xml` exclusions). Your data cannot leave the device
through Google's backup infrastructure.

## 3. When data leaves the device

Health data leaves the device only when **you** initiate one of the following
actions:

### 3.1 Cloud AI analysis (optional)
If you enable Cloud AI in Settings and supply your own API key, the app sends
a summary of the health entries you select to the AI provider you chose
(Anthropic Claude, OpenAI, Google Gemini, or DeepSeek) to generate a report.

- Network connections are HTTPS-only and restricted by
  `network_security_config.xml` to the provider domains listed above.
- Requests include the content of the report. Personal identifiers such as
  phone numbers and email addresses can be automatically redacted before
  sending — this setting is on by default.
- The AI provider's own privacy policy and data-handling terms apply to the
  request once it reaches them. Review:
  - Anthropic: https://www.anthropic.com/privacy
  - OpenAI: https://openai.com/policies/privacy-policy
  - Google Gemini: https://ai.google.dev/terms
  - DeepSeek: https://www.deepseek.com/privacy
- You can switch to on-device (Local) AI at any time, which performs analysis
  without any network request.

### 3.2 Share-to-doctor
When you tap "send report" the app opens your preferred SMS app with the
report pre-filled (using `ACTION_SENDTO`), or copies the report to the
clipboard. You must press "send" yourself. The app never sends messages on
your behalf.

### 3.3 Export
You can export the full journal as a ZIP archive (JSON + attachments) via the
system file picker. The archive is written only to the location you choose.

## 4. Sharing with third parties

We do not sell, rent, or share your health data with advertisers, data
brokers, insurers, or analytics providers. The only third parties that can
receive data are the AI providers you yourself configure (section 3.1).

## 5. Children

HealthJournal is not targeted at children under 13. Family profiles may
include children managed by an adult; the adult is responsible for the data
entered on a child's profile.

## 6. Your controls

Inside **Settings** you can:
- Enable or disable Cloud AI, and pick which provider (or Local only).
- Toggle phone/email redaction before Cloud AI requests.
- Turn on biometric / PIN app lock.
- View the audit log.
- **Export all data** to a ZIP archive.
- **Clear all data** — wipes the local database, preferences, audit log, and
  cached reports. This is irreversible.

Because we hold no server-side data, clearing the app's data is the complete
deletion mechanism.

## 7. Security

- App-sandbox isolation plus optional biometric / PIN screen lock.
- HTTPS-only enforcement via `network_security_config.xml`.
- API keys you enter are stored locally in DataStore and are never sent
  anywhere except to the provider you configured them for.
- Android automatic backup and D2D transfer are disabled for app data.

## 8. Medical disclaimer

HealthJournal is a personal journal. It does **not** provide medical advice,
diagnosis, or treatment. AI-generated reports are informational only and are
explicitly instructed to avoid diagnosis language. Always consult a qualified
healthcare professional for medical decisions.

## 9. Changes to this policy

If the policy changes, the updated version will ship with a new app release
and the "Last updated" date above will change.

## 10. Contact

Questions about this policy or requests regarding your data can be sent to
the developer contact listed on the Google Play store page for this app.
