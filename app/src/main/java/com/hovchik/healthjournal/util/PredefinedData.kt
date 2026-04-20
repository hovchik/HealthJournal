package com.hovchik.healthjournal.util

import com.hovchik.healthjournal.R

data class PredefinedItem(val key: String, val nameResId: Int)

object PredefinedData {

    val symptoms = listOf(
        // Head & Neurological
        PredefinedItem("headache", R.string.symptom_headache),
        PredefinedItem("migraine", R.string.symptom_migraine),
        PredefinedItem("dizziness", R.string.symptom_dizziness),
        PredefinedItem("blurred_vision", R.string.symptom_blurred_vision),
        PredefinedItem("earache", R.string.symptom_earache),
        PredefinedItem("tinnitus", R.string.symptom_tinnitus),
        PredefinedItem("numbness", R.string.symptom_numbness),
        PredefinedItem("tremor", R.string.symptom_tremor),
        PredefinedItem("memory_problems", R.string.symptom_memory_problems),
        PredefinedItem("confusion", R.string.symptom_confusion),
        // Respiratory
        PredefinedItem("cough", R.string.symptom_cough),
        PredefinedItem("sore_throat", R.string.symptom_sore_throat),
        PredefinedItem("runny_nose", R.string.symptom_runny_nose),
        PredefinedItem("sneezing", R.string.symptom_sneezing),
        PredefinedItem("shortness_of_breath", R.string.symptom_shortness_of_breath),
        PredefinedItem("wheezing", R.string.symptom_wheezing),
        PredefinedItem("nasal_congestion", R.string.symptom_nasal_congestion),
        // Cardiovascular
        PredefinedItem("chest_pain", R.string.symptom_chest_pain),
        PredefinedItem("palpitations", R.string.symptom_palpitations),
        PredefinedItem("swelling", R.string.symptom_swelling),
        PredefinedItem("cold_hands_feet", R.string.symptom_cold_hands_feet),
        // Gastrointestinal
        PredefinedItem("stomachache", R.string.symptom_stomachache),
        PredefinedItem("nausea", R.string.symptom_nausea),
        PredefinedItem("vomiting", R.string.symptom_vomiting),
        PredefinedItem("diarrhea", R.string.symptom_diarrhea),
        PredefinedItem("constipation", R.string.symptom_constipation),
        PredefinedItem("bloating", R.string.symptom_bloating),
        PredefinedItem("heartburn", R.string.symptom_heartburn),
        PredefinedItem("loss_of_appetite", R.string.symptom_loss_of_appetite),
        PredefinedItem("excessive_thirst", R.string.symptom_excessive_thirst),
        // Musculoskeletal
        PredefinedItem("back_pain", R.string.symptom_back_pain),
        PredefinedItem("joint_pain", R.string.symptom_joint_pain),
        PredefinedItem("muscle_pain", R.string.symptom_muscle_pain),
        PredefinedItem("neck_pain", R.string.symptom_neck_pain),
        PredefinedItem("stiffness", R.string.symptom_stiffness),
        PredefinedItem("muscle_cramps", R.string.symptom_muscle_cramps),
        // Dermatological
        PredefinedItem("rash", R.string.symptom_rash),
        PredefinedItem("itching", R.string.symptom_itching),
        PredefinedItem("dry_skin", R.string.symptom_dry_skin),
        PredefinedItem("bruising", R.string.symptom_bruising),
        PredefinedItem("hair_loss", R.string.symptom_hair_loss),
        // General / Systemic
        PredefinedItem("fatigue", R.string.symptom_fatigue),
        PredefinedItem("fever", R.string.symptom_fever),
        PredefinedItem("chills", R.string.symptom_chills),
        PredefinedItem("night_sweats", R.string.symptom_night_sweats),
        PredefinedItem("weight_loss", R.string.symptom_weight_loss),
        PredefinedItem("weight_gain", R.string.symptom_weight_gain),
        // Mental Health
        PredefinedItem("insomnia", R.string.symptom_insomnia),
        PredefinedItem("anxiety", R.string.symptom_anxiety),
        PredefinedItem("depression", R.string.symptom_depression),
        PredefinedItem("irritability", R.string.symptom_irritability),
        PredefinedItem("mood_swings", R.string.symptom_mood_swings),
        PredefinedItem("panic_attacks", R.string.symptom_panic_attacks),
        // Oral
        PredefinedItem("dry_mouth", R.string.symptom_dry_mouth),
        PredefinedItem("toothache", R.string.symptom_toothache),
        // Urological
        PredefinedItem("frequent_urination", R.string.symptom_frequent_urination),
        PredefinedItem("painful_urination", R.string.symptom_painful_urination)
    )

    val medications = listOf(
        // Pain / Anti-inflammatory
        PredefinedItem("ibuprofen", R.string.med_ibuprofen),
        PredefinedItem("paracetamol", R.string.med_paracetamol),
        PredefinedItem("aspirin", R.string.med_aspirin),
        PredefinedItem("diclofenac", R.string.med_diclofenac),
        PredefinedItem("naproxen", R.string.med_naproxen),
        PredefinedItem("tramadol", R.string.med_tramadol),
        // Antibiotics
        PredefinedItem("amoxicillin", R.string.med_amoxicillin),
        PredefinedItem("azithromycin", R.string.med_azithromycin),
        PredefinedItem("ciprofloxacin", R.string.med_ciprofloxacin),
        PredefinedItem("doxycycline", R.string.med_doxycycline),
        // Cardiovascular
        PredefinedItem("lisinopril", R.string.med_lisinopril),
        PredefinedItem("amlodipine", R.string.med_amlodipine),
        PredefinedItem("metoprolol", R.string.med_metoprolol),
        PredefinedItem("losartan", R.string.med_losartan),
        PredefinedItem("atorvastatin", R.string.med_atorvastatin),
        PredefinedItem("simvastatin", R.string.med_simvastatin),
        PredefinedItem("warfarin", R.string.med_warfarin),
        PredefinedItem("clopidogrel", R.string.med_clopidogrel),
        // GI
        PredefinedItem("omeprazole", R.string.med_omeprazole),
        PredefinedItem("pantoprazole", R.string.med_pantoprazole),
        PredefinedItem("metoclopramide", R.string.med_metoclopramide),
        // Diabetes
        PredefinedItem("metformin", R.string.med_metformin),
        PredefinedItem("insulin", R.string.med_insulin),
        // Thyroid
        PredefinedItem("levothyroxine", R.string.med_levothyroxine),
        // Allergy
        PredefinedItem("loratadine", R.string.med_loratadine),
        PredefinedItem("cetirizine", R.string.med_cetirizine),
        PredefinedItem("diphenhydramine", R.string.med_diphenhydramine),
        // Respiratory
        PredefinedItem("salbutamol", R.string.med_salbutamol),
        PredefinedItem("montelukast", R.string.med_montelukast),
        // Neuro / Psych
        PredefinedItem("gabapentin", R.string.med_gabapentin),
        PredefinedItem("sertraline", R.string.med_sertraline),
        PredefinedItem("amitriptyline", R.string.med_amitriptyline),
        PredefinedItem("diazepam", R.string.med_diazepam),
        // Corticosteroids
        PredefinedItem("prednisone", R.string.med_prednisone),
        PredefinedItem("dexamethasone", R.string.med_dexamethasone),
        // Supplements
        PredefinedItem("vitamin_d", R.string.med_vitamin_d),
        PredefinedItem("vitamin_c", R.string.med_vitamin_c),
        PredefinedItem("vitamin_b12", R.string.med_vitamin_b12),
        PredefinedItem("omega3", R.string.med_omega3),
        PredefinedItem("magnesium", R.string.med_magnesium),
        PredefinedItem("iron", R.string.med_iron),
        PredefinedItem("folic_acid", R.string.med_folic_acid),
        PredefinedItem("calcium", R.string.med_calcium),
        PredefinedItem("zinc", R.string.med_zinc),
        PredefinedItem("probiotics", R.string.med_probiotics),
        PredefinedItem("melatonin", R.string.med_melatonin),
        PredefinedItem("multivitamin", R.string.med_multivitamin)
    )

    val relationships = listOf(
        R.string.rel_spouse,
        R.string.rel_child,
        R.string.rel_parent,
        R.string.rel_sibling,
        R.string.rel_grandparent,
        R.string.rel_grandchild,
        R.string.rel_uncle_aunt,
        R.string.rel_cousin,
        R.string.rel_friend,
        R.string.rel_other
    )

    val relationshipItems = listOf(
        PredefinedItem("spouse", R.string.rel_spouse),
        PredefinedItem("child", R.string.rel_child),
        PredefinedItem("parent", R.string.rel_parent),
        PredefinedItem("sibling", R.string.rel_sibling),
        PredefinedItem("grandparent", R.string.rel_grandparent),
        PredefinedItem("grandchild", R.string.rel_grandchild),
        PredefinedItem("uncle_aunt", R.string.rel_uncle_aunt),
        PredefinedItem("cousin", R.string.rel_cousin),
        PredefinedItem("friend", R.string.rel_friend),
        PredefinedItem("other", R.string.rel_other)
    )

    val groupItems = listOf(
        PredefinedItem("chronic", R.string.group_chronic),
        PredefinedItem("seasonal", R.string.group_seasonal),
        PredefinedItem("allergies", R.string.group_allergies),
        PredefinedItem("infections", R.string.group_infections),
        PredefinedItem("cardiovascular", R.string.group_cardiovascular),
        PredefinedItem("respiratory", R.string.group_respiratory),
        PredefinedItem("gastrointestinal", R.string.group_gastrointestinal),
        PredefinedItem("neurological", R.string.group_neurological),
        PredefinedItem("musculoskeletal", R.string.group_musculoskeletal),
        PredefinedItem("dermatological", R.string.group_dermatological),
        PredefinedItem("mental_health", R.string.group_mental_health),
        PredefinedItem("endocrine", R.string.group_endocrine),
        PredefinedItem("urological", R.string.group_urological),
        PredefinedItem("dental", R.string.group_dental),
        PredefinedItem("eye", R.string.group_eye),
        PredefinedItem("other", R.string.group_other)
    )
}
