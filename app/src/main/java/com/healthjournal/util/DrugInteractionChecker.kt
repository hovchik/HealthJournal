package com.healthjournal.util

import com.healthjournal.domain.model.DrugInteraction
import com.healthjournal.domain.model.InteractionSeverity
import com.healthjournal.domain.model.Medication

object DrugInteractionChecker {

    private val knownInteractions = listOf(
        DrugInteraction("warfarin", "aspirin", InteractionSeverity.MAJOR, "Increased bleeding risk"),
        DrugInteraction("warfarin", "ibuprofen", InteractionSeverity.MAJOR, "Increased bleeding risk"),
        DrugInteraction("warfarin", "naproxen", InteractionSeverity.MAJOR, "Increased bleeding risk"),
        DrugInteraction("metformin", "alcohol", InteractionSeverity.MAJOR, "Risk of lactic acidosis"),
        DrugInteraction("lisinopril", "losartan", InteractionSeverity.MAJOR, "Dual RAAS blockade risk"),
        DrugInteraction("sertraline", "tramadol", InteractionSeverity.MAJOR, "Serotonin syndrome risk"),
        DrugInteraction("metoprolol", "amlodipine", InteractionSeverity.MODERATE, "Excessive blood pressure lowering"),
        DrugInteraction("omeprazole", "clopidogrel", InteractionSeverity.MAJOR, "Reduced antiplatelet effect"),
        DrugInteraction("simvastatin", "amlodipine", InteractionSeverity.MODERATE, "Increased statin levels"),
        DrugInteraction("gabapentin", "tramadol", InteractionSeverity.MODERATE, "CNS depression risk"),
        DrugInteraction("diazepam", "tramadol", InteractionSeverity.MAJOR, "Respiratory depression risk"),
        DrugInteraction("ciprofloxacin", "calcium", InteractionSeverity.MODERATE, "Reduced antibiotic absorption"),
        DrugInteraction("levothyroxine", "calcium", InteractionSeverity.MODERATE, "Reduced thyroid hormone absorption"),
        DrugInteraction("levothyroxine", "iron", InteractionSeverity.MODERATE, "Reduced thyroid hormone absorption"),
        DrugInteraction("metformin", "dexamethasone", InteractionSeverity.MODERATE, "Steroid may increase blood sugar"),
        DrugInteraction("insulin", "prednisone", InteractionSeverity.MODERATE, "Steroid may increase blood sugar"),
        DrugInteraction("insulin", "dexamethasone", InteractionSeverity.MODERATE, "Steroid may increase blood sugar"),
        DrugInteraction("aspirin", "ibuprofen", InteractionSeverity.MODERATE, "Reduced aspirin cardioprotection"),
        DrugInteraction("loratadine", "amitriptyline", InteractionSeverity.MINOR, "Increased anticholinergic effects"),
        DrugInteraction("cetirizine", "diazepam", InteractionSeverity.MINOR, "Increased sedation"),
    )

    fun checkInteractions(medications: List<Medication>): List<DrugInteraction> {
        val activeNames = medications.filter { it.active }.map { it.name.lowercase().trim() }
        val found = mutableListOf<DrugInteraction>()

        for (i in activeNames.indices) {
            for (j in i + 1 until activeNames.size) {
                val name1 = activeNames[i]
                val name2 = activeNames[j]

                for (interaction in knownInteractions) {
                    if ((name1.contains(interaction.drug1) && name2.contains(interaction.drug2)) ||
                        (name1.contains(interaction.drug2) && name2.contains(interaction.drug1))
                    ) {
                        found.add(interaction)
                    }
                }
            }
        }
        return found
    }
}
