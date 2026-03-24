package com.healthjournal.util

import com.healthjournal.domain.model.*
import kotlinx.serialization.json.*
import java.time.format.DateTimeFormatter

object FhirExporter {

    private val isoFormatter = DateTimeFormatter.ISO_DATE_TIME

    fun exportBundle(
        symptoms: List<Symptom>,
        vitals: List<VitalSign>,
        medications: List<Medication>,
        patientName: String = "Patient"
    ): String {
        val entries = mutableListOf<JsonObject>()

        // Patient resource
        entries.add(buildJsonObject {
            put("fullUrl", JsonPrimitive("urn:uuid:patient-1"))
            put("resource", buildJsonObject {
                put("resourceType", JsonPrimitive("Patient"))
                put("name", buildJsonArray {
                    add(buildJsonObject {
                        put("text", JsonPrimitive(patientName))
                    })
                })
            })
        })

        // Observations from vitals
        for (vital in vitals) {
            entries.add(buildJsonObject {
                put("fullUrl", JsonPrimitive("urn:uuid:vital-${vital.id}"))
                put("resource", buildJsonObject {
                    put("resourceType", JsonPrimitive("Observation"))
                    put("status", JsonPrimitive("final"))
                    put("code", buildJsonObject {
                        put("text", JsonPrimitive(vital.type.displayName))
                    })
                    put("effectiveDateTime", JsonPrimitive(vital.recordedAt.format(isoFormatter)))
                    put("valueQuantity", buildJsonObject {
                        put("value", JsonPrimitive(vital.value))
                        put("unit", JsonPrimitive(vital.type.unit))
                    })
                    if (vital.secondaryValue != null) {
                        put("component", buildJsonArray {
                            add(buildJsonObject {
                                put("code", buildJsonObject { put("text", JsonPrimitive("diastolic")) })
                                put("valueQuantity", buildJsonObject {
                                    put("value", JsonPrimitive(vital.secondaryValue))
                                    put("unit", JsonPrimitive(vital.type.unit))
                                })
                            })
                        })
                    }
                })
            })
        }

        // Conditions from symptoms
        for (symptom in symptoms) {
            entries.add(buildJsonObject {
                put("fullUrl", JsonPrimitive("urn:uuid:symptom-${symptom.id}"))
                put("resource", buildJsonObject {
                    put("resourceType", JsonPrimitive("Condition"))
                    put("clinicalStatus", buildJsonObject {
                        put("coding", buildJsonArray {
                            add(buildJsonObject {
                                put("code", JsonPrimitive("active"))
                            })
                        })
                    })
                    put("code", buildJsonObject {
                        put("text", JsonPrimitive(symptom.name))
                    })
                    put("severity", buildJsonObject {
                        put("text", JsonPrimitive("${symptom.intensity}/10"))
                    })
                    put("recordedDate", JsonPrimitive(symptom.recordedAt.format(isoFormatter)))
                    if (symptom.notes.isNotBlank()) {
                        put("note", buildJsonArray {
                            add(buildJsonObject { put("text", JsonPrimitive(symptom.notes)) })
                        })
                    }
                })
            })
        }

        // MedicationStatements
        for (med in medications) {
            entries.add(buildJsonObject {
                put("fullUrl", JsonPrimitive("urn:uuid:med-${med.id}"))
                put("resource", buildJsonObject {
                    put("resourceType", JsonPrimitive("MedicationStatement"))
                    put("status", JsonPrimitive(if (med.active) "active" else "completed"))
                    put("medicationCodeableConcept", buildJsonObject {
                        put("text", JsonPrimitive(med.name))
                    })
                    put("dosage", buildJsonArray {
                        add(buildJsonObject {
                            put("text", JsonPrimitive("${med.dosage} - ${med.frequency}"))
                        })
                    })
                })
            })
        }

        val bundle = buildJsonObject {
            put("resourceType", JsonPrimitive("Bundle"))
            put("type", JsonPrimitive("collection"))
            put("entry", buildJsonArray { entries.forEach { add(it) } })
        }

        return Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), bundle)
    }
}
