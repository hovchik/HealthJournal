package com.hovchik.healthjournal.util

import android.content.Context
import android.net.Uri
import com.hovchik.healthjournal.data.local.JsonFileStore
import com.hovchik.healthjournal.data.local.dto.*
import com.hovchik.healthjournal.domain.model.ai.AiSettings
import com.hovchik.healthjournal.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BackupData(
    val meta: ExportMeta = ExportMeta(),
    val symptoms: List<SymptomDto> = emptyList(),
    val vitalSigns: List<VitalSignDto> = emptyList(),
    val medications: List<MedicationDto> = emptyList(),
    val medicationLogs: List<MedicationLogDto> = emptyList(),
    val aiReports: List<AiReportDto> = emptyList(),
    val familyMembers: List<FamilyMemberDto> = emptyList(),
    val diseases: List<DiseaseDto> = emptyList(),
    val appointments: List<AppointmentDto> = emptyList(),
    val reminders: List<ReminderDto> = emptyList(),
    val doctorContacts: List<DoctorContactDto> = emptyList(),
    val auditLog: List<AuditLogDto> = emptyList(),
    val userSettings: UserSettingsBackup? = null,
    val predefinedData: PredefinedDataBackup? = null
)

@Serializable
data class UserSettingsBackup(
    val userName: String = "",
    val doctorName: String = "",
    val doctorPhone: String = "",
    val aiConsentGiven: Boolean = false,
    val languageMode: String = "SYSTEM",
    val themeMode: String = "SYSTEM",
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val gender: String = "",
    val knownDiseases: List<String> = emptyList(),
    val activeProfileId: Long = 0,
    val aiSettingsJson: String? = null
)

@Serializable
data class PredefinedDataBackup(
    val disabledSymptoms: List<String> = emptyList(),
    val disabledMedications: List<String> = emptyList(),
    val disabledRelations: List<String> = emptyList(),
    val disabledGroups: List<String> = emptyList(),
    val customSymptoms: List<String> = emptyList(),
    val customMedications: List<String> = emptyList(),
    val customRelations: List<String> = emptyList(),
    val customGroups: List<String> = emptyList(),
    val enabledSymptomKeys: List<String> = emptyList(),
    val enabledMedicationKeys: List<String> = emptyList(),
    val enabledRelationKeys: List<String> = emptyList(),
    val enabledGroupKeys: List<String> = emptyList()
)

class DataExportImportManager(
    private val context: Context,
    private val symptomStore: JsonFileStore<SymptomDto>,
    private val vitalSignStore: JsonFileStore<VitalSignDto>,
    private val medicationStore: JsonFileStore<MedicationDto>,
    private val medicationLogStore: JsonFileStore<MedicationLogDto>,
    private val aiReportStore: JsonFileStore<AiReportDto>,
    private val familyMemberStore: JsonFileStore<FamilyMemberDto>,
    private val diseaseStore: JsonFileStore<DiseaseDto>,
    private val appointmentStore: JsonFileStore<AppointmentDto>,
    private val reminderStore: JsonFileStore<ReminderDto>,
    private val doctorContactStore: JsonFileStore<DoctorContactDto>,
    private val auditLogStore: JsonFileStore<AuditLogDto>,
    private val userSettingsRepository: UserSettingsRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private suspend fun buildBackupData(): BackupData {
        val settings = userSettingsRepository.getUserSettings().first()

        val predefinedPrefs = context.predefinedDataStore.data.first()
        val disabledSymptoms = predefinedPrefs[PredefinedDataKeys.DISABLED_SYMPTOMS] ?: emptySet()
        val disabledMedications = predefinedPrefs[PredefinedDataKeys.DISABLED_MEDICATIONS] ?: emptySet()
        val disabledRelations = predefinedPrefs[PredefinedDataKeys.DISABLED_RELATIONS] ?: emptySet()
        val customSymptoms = predefinedPrefs[PredefinedDataKeys.CUSTOM_SYMPTOMS] ?: emptySet()
        val customMedications = predefinedPrefs[PredefinedDataKeys.CUSTOM_MEDICATIONS] ?: emptySet()
        val customRelations = predefinedPrefs[PredefinedDataKeys.CUSTOM_RELATIONS] ?: emptySet()
        val disabledGroups = predefinedPrefs[PredefinedDataKeys.DISABLED_GROUPS] ?: emptySet()
        val customGroups = predefinedPrefs[PredefinedDataKeys.CUSTOM_GROUPS] ?: emptySet()

        val predefinedData = PredefinedDataBackup(
            disabledSymptoms = disabledSymptoms.toList(),
            disabledMedications = disabledMedications.toList(),
            disabledRelations = disabledRelations.toList(),
            disabledGroups = disabledGroups.toList(),
            customSymptoms = customSymptoms.toList(),
            customMedications = customMedications.toList(),
            customRelations = customRelations.toList(),
            customGroups = customGroups.toList(),
            enabledSymptomKeys = PredefinedData.symptoms
                .filter { it.key !in disabledSymptoms }
                .map { it.key } + customSymptoms.toList(),
            enabledMedicationKeys = PredefinedData.medications
                .filter { it.key !in disabledMedications }
                .map { it.key } + customMedications.toList(),
            enabledRelationKeys = PredefinedData.relationshipItems
                .filter { it.key !in disabledRelations }
                .map { it.key } + customRelations.toList(),
            enabledGroupKeys = PredefinedData.groupItems
                .filter { it.key !in disabledGroups }
                .map { it.key } + customGroups.toList()
        )

        return BackupData(
            meta = ExportMeta(uiLanguage = settings.languageMode),
            symptoms = symptomStore.getAll(),
            vitalSigns = vitalSignStore.getAll(),
            medications = medicationStore.getAll(),
            medicationLogs = medicationLogStore.getAll(),
            aiReports = aiReportStore.getAll(),
            familyMembers = familyMemberStore.getAll(),
            diseases = diseaseStore.getAll(),
            appointments = appointmentStore.getAll(),
            reminders = reminderStore.getAll(),
            doctorContacts = doctorContactStore.getAll(),
            auditLog = auditLogStore.getAll(),
            userSettings = UserSettingsBackup(
                userName = settings.userName,
                doctorName = settings.doctorName,
                doctorPhone = settings.doctorPhone,
                aiConsentGiven = settings.aiConsentGiven,
                languageMode = settings.languageMode,
                themeMode = settings.themeMode,
                weight = settings.weight,
                height = settings.height,
                age = settings.age,
                gender = settings.gender,
                knownDiseases = settings.knownDiseases,
                activeProfileId = settings.activeProfileId,
                aiSettingsJson = try {
                    json.encodeToString(AiSettings.serializer(), settings.aiSettings)
                } catch (_: Exception) { null }
            ),
            predefinedData = predefinedData
        )
    }

    suspend fun exportToString(): String {
        val backup = buildBackupData()
        return json.encodeToString(BackupData.serializer(), backup)
    }

    suspend fun exportData(uri: Uri) {
        val jsonString = exportToString()
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(jsonString.toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("Cannot open output stream")
    }

    suspend fun importFromString(jsonString: String) {
        val backup = json.decodeFromString(BackupData.serializer(), jsonString)
        restoreBackup(backup)
    }

    suspend fun clearAllData() {
        symptomStore.replaceAll(emptyList())
        vitalSignStore.replaceAll(emptyList())
        medicationStore.replaceAll(emptyList())
        medicationLogStore.replaceAll(emptyList())
        aiReportStore.replaceAll(emptyList())
        familyMemberStore.replaceAll(emptyList())
        diseaseStore.replaceAll(emptyList())
        appointmentStore.replaceAll(emptyList())
        reminderStore.replaceAll(emptyList())
        doctorContactStore.replaceAll(emptyList())
        auditLogStore.replaceAll(emptyList())
    }

    suspend fun importData(uri: Uri) {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IllegalStateException("Cannot open input stream")

        importFromString(jsonString)
    }

    private suspend fun restoreBackup(backup: BackupData) {
        // Replace all data stores
        symptomStore.replaceAll(backup.symptoms)
        vitalSignStore.replaceAll(backup.vitalSigns)
        medicationStore.replaceAll(backup.medications)
        medicationLogStore.replaceAll(backup.medicationLogs)
        aiReportStore.replaceAll(backup.aiReports)
        familyMemberStore.replaceAll(backup.familyMembers)
        diseaseStore.replaceAll(backup.diseases)
        appointmentStore.replaceAll(backup.appointments)
        reminderStore.replaceAll(backup.reminders)
        doctorContactStore.replaceAll(backup.doctorContacts)
        auditLogStore.replaceAll(backup.auditLog)

        // Restore user settings (keep onboarding completed)
        backup.userSettings?.let { settingsBackup ->
            val current = userSettingsRepository.getUserSettings().first()
            val restoredAiSettings = settingsBackup.aiSettingsJson?.let { aiJson ->
                try { json.decodeFromString<AiSettings>(aiJson) } catch (_: Exception) { null }
            } ?: current.aiSettings
            userSettingsRepository.updateUserSettings(
                current.copy(
                    userName = settingsBackup.userName,
                    doctorName = settingsBackup.doctorName,
                    doctorPhone = settingsBackup.doctorPhone,
                    aiConsentGiven = settingsBackup.aiConsentGiven,
                    languageMode = settingsBackup.languageMode,
                    themeMode = settingsBackup.themeMode,
                    weight = settingsBackup.weight,
                    height = settingsBackup.height,
                    age = settingsBackup.age,
                    gender = settingsBackup.gender,
                    knownDiseases = settingsBackup.knownDiseases,
                    activeProfileId = settingsBackup.activeProfileId,
                    aiSettings = restoredAiSettings
                )
            )
        }

        // Restore predefined data settings
        backup.predefinedData?.let { pd ->
            context.predefinedDataStore.updateData { prefs ->
                val mutable = prefs.toMutablePreferences()
                mutable[PredefinedDataKeys.DISABLED_SYMPTOMS] = pd.disabledSymptoms.toSet()
                mutable[PredefinedDataKeys.DISABLED_MEDICATIONS] = pd.disabledMedications.toSet()
                mutable[PredefinedDataKeys.DISABLED_RELATIONS] = pd.disabledRelations.toSet()
                mutable[PredefinedDataKeys.CUSTOM_SYMPTOMS] = pd.customSymptoms.toSet()
                mutable[PredefinedDataKeys.CUSTOM_MEDICATIONS] = pd.customMedications.toSet()
                mutable[PredefinedDataKeys.CUSTOM_RELATIONS] = pd.customRelations.toSet()
                mutable[PredefinedDataKeys.DISABLED_GROUPS] = pd.disabledGroups.toSet()
                mutable[PredefinedDataKeys.CUSTOM_GROUPS] = pd.customGroups.toSet()
                mutable
            }
        }
    }
}
