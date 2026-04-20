package com.hovchik.healthjournal.presentation.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hovchik.healthjournal.R
import com.hovchik.healthjournal.domain.model.Disease
import com.hovchik.healthjournal.presentation.screen.disease.DiseaseViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletedDiseasesScreen(
    onBack: () -> Unit,
    viewModel: DiseaseViewModel = viewModel()
) {
    val deletedDiseases by viewModel.deletedDiseases.collectAsStateWithLifecycle()
    var confirmDeleteDisease by remember { mutableStateOf<Disease?>(null) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.deleted_diseases),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (deletedDiseases.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.no_deleted_diseases_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(deletedDiseases, key = { it.id }) { disease ->
                    DeletedDiseaseItem(
                        disease = disease,
                        dateFormatter = dateFormatter,
                        onRestore = { viewModel.restoreDisease(disease) },
                        onPermanentDelete = { confirmDeleteDisease = disease }
                    )
                }
            }
        }
    }

    confirmDeleteDisease?.let { disease ->
        AlertDialog(
            onDismissRequest = { confirmDeleteDisease = null },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
            },
            title = {
                Text(stringResource(R.string.permanently_delete),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            },
            text = {
                Text(stringResource(R.string.confirm_permanent_delete),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.permanentlyDeleteDisease(disease)
                        confirmDeleteDisease = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = MaterialTheme.shapes.medium
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirmDeleteDisease = null },
                    shape = MaterialTheme.shapes.medium
                ) { Text(stringResource(R.string.cancel)) }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

@Composable
private fun DeletedDiseaseItem(
    disease: Disease,
    dateFormatter: DateTimeFormatter,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    disease.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (disease.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        disease.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.created_date, disease.createdAt.format(dateFormatter)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    stringResource(R.string.deleted_date, disease.deletedAt?.format(dateFormatter) ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onRestore) {
                Icon(
                    Icons.Outlined.RestoreFromTrash,
                    contentDescription = stringResource(R.string.restore),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onPermanentDelete) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.permanently_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
