package com.healthjournal.util

import com.healthjournal.R

data class PredefinedItem(val nameResId: Int)

object PredefinedData {

    val symptoms = listOf(
        PredefinedItem(R.string.symptom_headache),
        PredefinedItem(R.string.symptom_stomachache),
        PredefinedItem(R.string.symptom_back_pain),
        PredefinedItem(R.string.symptom_fatigue),
        PredefinedItem(R.string.symptom_dizziness),
        PredefinedItem(R.string.symptom_nausea),
        PredefinedItem(R.string.symptom_insomnia),
        PredefinedItem(R.string.symptom_cough),
        PredefinedItem(R.string.symptom_sore_throat),
        PredefinedItem(R.string.symptom_runny_nose),
        PredefinedItem(R.string.symptom_joint_pain),
        PredefinedItem(R.string.symptom_chest_pain),
        PredefinedItem(R.string.symptom_shortness_of_breath),
        PredefinedItem(R.string.symptom_fever),
        PredefinedItem(R.string.symptom_anxiety)
    )

    val medications = listOf(
        PredefinedItem(R.string.med_ibuprofen),
        PredefinedItem(R.string.med_paracetamol),
        PredefinedItem(R.string.med_aspirin),
        PredefinedItem(R.string.med_amoxicillin),
        PredefinedItem(R.string.med_omeprazole),
        PredefinedItem(R.string.med_metformin),
        PredefinedItem(R.string.med_lisinopril),
        PredefinedItem(R.string.med_amlodipine),
        PredefinedItem(R.string.med_metoprolol),
        PredefinedItem(R.string.med_loratadine),
        PredefinedItem(R.string.med_vitamin_d),
        PredefinedItem(R.string.med_vitamin_c),
        PredefinedItem(R.string.med_omega3)
    )

    val relationships = listOf(
        R.string.rel_self,
        R.string.rel_spouse,
        R.string.rel_child,
        R.string.rel_parent,
        R.string.rel_sibling,
        R.string.rel_grandparent
    )
}
