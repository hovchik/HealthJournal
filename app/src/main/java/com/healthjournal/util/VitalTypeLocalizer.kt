package com.healthjournal.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.healthjournal.R
import com.healthjournal.domain.model.VitalType

@get:StringRes
val VitalType.displayNameResId: Int
    get() = when (this) {
        VitalType.BLOOD_PRESSURE -> R.string.vital_blood_pressure
        VitalType.PULSE -> R.string.vital_pulse
        VitalType.TEMPERATURE -> R.string.vital_temperature
        VitalType.SPO2 -> R.string.vital_spo2
        VitalType.GLUCOSE -> R.string.vital_glucose
        VitalType.WEIGHT -> R.string.vital_weight
        VitalType.SLEEP -> R.string.vital_sleep
        VitalType.STEPS -> R.string.vital_steps
    }

@get:StringRes
val VitalType.unitResId: Int
    get() = when (this) {
        VitalType.BLOOD_PRESSURE -> R.string.unit_mmhg
        VitalType.PULSE -> R.string.unit_bpm
        VitalType.TEMPERATURE -> R.string.unit_celsius
        VitalType.SPO2 -> R.string.unit_percent
        VitalType.GLUCOSE -> R.string.unit_mmol
        VitalType.WEIGHT -> R.string.unit_kg
        VitalType.SLEEP -> R.string.unit_hours
        VitalType.STEPS -> R.string.unit_steps
    }

@Composable
fun VitalType.localizedDisplayName(): String = stringResource(displayNameResId)

@Composable
fun VitalType.localizedUnit(): String = stringResource(unitResId)
