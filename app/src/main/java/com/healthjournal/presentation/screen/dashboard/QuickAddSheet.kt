package com.healthjournal.presentation.screen.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.healthjournal.R
import com.healthjournal.presentation.theme.LocalHealthExtended

/*
 * Sheet-as-index. No rows-as-tiles — each action is a single line of serif
 * body text + a tiny Grotesque metadata numeral, separated by hairline rules.
 * The sheet itself is the only surface with alpha; the rows are flat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onAddSymptom: () -> Unit,
    onAddVital: () -> Unit,
    onAddMedication: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ext = LocalHealthExtended.current

    // Run the action first so the destination takes over while this sheet
    // dismisses — otherwise `rememberCoroutineScope` here can be cancelled.
    fun runThenDismiss(action: () -> Unit) {
        action()
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ext.paper,
        contentColor = ext.ink,
        shape = MaterialTheme.shapes.extraLarge // = 0.dp from the theme
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = EditorialMargin,
                    vertical = 8.dp
                )
            ) {
                Text(
                    text = stringResource(R.string.quick_add_title).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.muted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.quick_add_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = ext.ink
                )
            }

            Spacer(Modifier.height(8.dp))
            InkRule(weight = 1f)

            QuickAddLine(
                index = "01",
                icon = Icons.Default.Sick,
                label = stringResource(R.string.add_symptom_title),
                onClick = { runThenDismiss(onAddSymptom) }
            )
            HairlineRule()
            QuickAddLine(
                index = "02",
                icon = Icons.Default.MonitorHeart,
                label = stringResource(R.string.add_vital_title),
                onClick = { runThenDismiss(onAddVital) }
            )
            HairlineRule()
            QuickAddLine(
                index = "03",
                icon = Icons.Default.Medication,
                label = stringResource(R.string.add_medication_title),
                onClick = { runThenDismiss(onAddMedication) }
            )
            HairlineRule()

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickAddLine(
    index: String,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val ext = LocalHealthExtended.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = EditorialMargin, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = index,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = ext.numerals),
            color = ext.muted
        )
        // Ink-only icon, no background chip
        Icon(
            icon,
            contentDescription = null,
            tint = ext.ink,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = ext.ink
        )
    }
}
