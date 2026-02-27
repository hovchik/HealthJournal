package com.healthjournal.util

import com.healthjournal.R

data class PredefinedItem(val key: String, val nameResId: Int)

object PredefinedData {

    val symptoms = listOf(
        PredefinedItem("headache", R.string.symptom_headache),
        PredefinedItem("stomachache", R.string.symptom_stomachache),
        PredefinedItem("back_pain", R.string.symptom_back_pain),
        PredefinedItem("fatigue", R.string.symptom_fatigue),
        PredefinedItem("dizziness", R.string.symptom_dizziness),
        PredefinedItem("nausea", R.string.symptom_nausea),
        PredefinedItem("insomnia", R.string.symptom_insomnia),
        PredefinedItem("cough", R.string.symptom_cough),
        PredefinedItem("sore_throat", R.string.symptom_sore_throat),
        PredefinedItem("runny_nose", R.string.symptom_runny_nose),
        PredefinedItem("joint_pain", R.string.symptom_joint_pain),
        PredefinedItem("chest_pain", R.string.symptom_chest_pain),
        PredefinedItem("shortness_of_breath", R.string.symptom_shortness_of_breath),
        PredefinedItem("fever", R.string.symptom_fever),
        PredefinedItem("anxiety", R.string.symptom_anxiety),
        PredefinedItem("muscle_pain", R.string.symptom_muscle_pain),
        PredefinedItem("rash", R.string.symptom_rash),
        PredefinedItem("palpitations", R.string.symptom_palpitations),
        PredefinedItem("blurred_vision", R.string.symptom_blurred_vision),
        PredefinedItem("earache", R.string.symptom_earache),
        PredefinedItem("constipation", R.string.symptom_constipation),
        PredefinedItem("diarrhea", R.string.symptom_diarrhea),
        PredefinedItem("bloating", R.string.symptom_bloating),
        PredefinedItem("numbness", R.string.symptom_numbness),
        PredefinedItem("swelling", R.string.symptom_swelling),
        PredefinedItem("depression", R.string.symptom_depression),
        PredefinedItem("heartburn", R.string.symptom_heartburn),
        PredefinedItem("loss_of_appetite", R.string.symptom_loss_of_appetite),
        PredefinedItem("sneezing", R.string.symptom_sneezing),
        PredefinedItem("dry_mouth", R.string.symptom_dry_mouth)
    )

    val medications = listOf(
        PredefinedItem("ibuprofen", R.string.med_ibuprofen),
        PredefinedItem("paracetamol", R.string.med_paracetamol),
        PredefinedItem("aspirin", R.string.med_aspirin),
        PredefinedItem("amoxicillin", R.string.med_amoxicillin),
        PredefinedItem("omeprazole", R.string.med_omeprazole),
        PredefinedItem("metformin", R.string.med_metformin),
        PredefinedItem("lisinopril", R.string.med_lisinopril),
        PredefinedItem("amlodipine", R.string.med_amlodipine),
        PredefinedItem("metoprolol", R.string.med_metoprolol),
        PredefinedItem("loratadine", R.string.med_loratadine),
        PredefinedItem("vitamin_d", R.string.med_vitamin_d),
        PredefinedItem("vitamin_c", R.string.med_vitamin_c),
        PredefinedItem("omega3", R.string.med_omega3),
        PredefinedItem("atorvastatin", R.string.med_atorvastatin),
        PredefinedItem("levothyroxine", R.string.med_levothyroxine),
        PredefinedItem("losartan", R.string.med_losartan),
        PredefinedItem("gabapentin", R.string.med_gabapentin),
        PredefinedItem("pantoprazole", R.string.med_pantoprazole),
        PredefinedItem("cetirizine", R.string.med_cetirizine),
        PredefinedItem("prednisone", R.string.med_prednisone),
        PredefinedItem("diclofenac", R.string.med_diclofenac),
        PredefinedItem("magnesium", R.string.med_magnesium),
        PredefinedItem("probiotics", R.string.med_probiotics),
        PredefinedItem("iron", R.string.med_iron),
        PredefinedItem("folic_acid", R.string.med_folic_acid),
        PredefinedItem("melatonin", R.string.med_melatonin)
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
