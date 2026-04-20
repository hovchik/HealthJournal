package com.healthjournal.presentation.screen.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.healthjournal.R
import com.healthjournal.presentation.theme.LocalHealthExtended

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onAddSymptom: () -> Unit,
    onAddVital: () -> Unit,
    onAddMedication: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cs = MaterialTheme.colorScheme

    // Navigation before dismiss: the destination composable will take over
    // while the sheet animates out as part of unmounting the dashboard.
    fun runThenDismiss(action: () -> Unit) {
        action()
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = cs.surface,
        contentColor = cs.onSurface,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.quick_add_title),
                style = MaterialTheme.typography.headlineSmall,
                color = cs.onSurface
            )
            Spacer(Modifier.size(16.dp))

            QuickAddRow(
                icon = Icons.Default.Sick,
                label = stringResource(R.string.add_symptom_title),
                accent = cs.error,
                onClick = { runThenDismiss(onAddSymptom) }
            )
            Spacer(Modifier.size(8.dp))
            QuickAddRow(
                icon = Icons.Default.MonitorHeart,
                label = stringResource(R.string.add_vital_title),
                accent = cs.tertiary,
                onClick = { runThenDismiss(onAddVital) }
            )
            Spacer(Modifier.size(8.dp))
            QuickAddRow(
                icon = Icons.Default.Medication,
                label = stringResource(R.string.add_medication_title),
                accent = cs.primary,
                onClick = { runThenDismiss(onAddMedication) }
            )
            Spacer(Modifier.size(20.dp))
        }
    }
}

@Composable
private fun QuickAddRow(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val ext = LocalHealthExtended.current
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = cs.surface,
        border = BorderStroke(1.dp, ext.gridInk),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = cs.onSurface
            )
        }
    }
}
