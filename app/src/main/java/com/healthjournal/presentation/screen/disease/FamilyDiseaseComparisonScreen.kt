package com.healthjournal.presentation.screen.disease

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthjournal.R
import com.healthjournal.domain.model.Disease
import com.healthjournal.domain.model.FamilyMember

data class DiseaseGroup(
    val diseaseName: String,
    val entries: List<DiseaseWithProfile>
)

data class DiseaseWithProfile(
    val disease: Disease,
    val profileName: String,
    val symptomCount: Int,
    val vitalCount: Int,
    val medicationCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDiseaseComparisonScreen(
    onBack: () -> Unit,
    viewModel: DiseaseViewModel = viewModel()
) {
    val allDiseases by viewModel.allDiseases.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val allSymptoms by viewModel.allSymptoms.collectAsStateWithLifecycle()
    val allVitals by viewModel.allVitals.collectAsStateWithLifecycle()
    val allMedications by viewModel.allMedications.collectAsStateWithLifecycle()

    val selfLabel = stringResource(R.string.rel_self)

    val diseaseGroups = remember(allDiseases, familyMembers, allSymptoms, allVitals, allMedications) {
        allDiseases
            .groupBy { it.name.lowercase().trim() }
            .filter { it.value.size > 1 || it.value.any { d -> d.profileId != 0L } }
            .map { (_, diseases) ->
                DiseaseGroup(
                    diseaseName = diseases.first().name,
                    entries = diseases.map { disease ->
                        val profileName = if (disease.profileId == 0L) selfLabel
                            else familyMembers.find { it.id == disease.profileId }?.name ?: selfLabel
                        DiseaseWithProfile(
                            disease = disease,
                            profileName = profileName,
                            symptomCount = allSymptoms.count { it.diseaseId == disease.id },
                            vitalCount = allVitals.count { it.diseaseId == disease.id },
                            medicationCount = allMedications.count { it.diseaseId == disease.id }
                        )
                    }
                )
            }
            .sortedByDescending { it.entries.size }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.family_diseases_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        if (diseaseGroups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FamilyRestroom, contentDescription = null,
                                modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.no_family_diseases_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(diseaseGroups, key = { it.diseaseName }) { group ->
                    DiseaseGroupCard(group)
                }
            }
        }
    }
}

@Composable
private fun DiseaseGroupCard(group: DiseaseGroup) {
    val profileColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Text(group.diseaseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                    Text(
                        stringResource(R.string.family_members_count, group.entries.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            group.entries.forEachIndexed { index, entry ->
                val color = profileColors[index % profileColors.size]

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = color.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                entry.profileName.first().uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.profileName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CompactStat(Icons.Default.Sick, entry.symptomCount, MaterialTheme.colorScheme.primary)
                            CompactStat(Icons.Default.MonitorHeart, entry.vitalCount, MaterialTheme.colorScheme.tertiary)
                            CompactStat(Icons.Default.Medication, entry.medicationCount, MaterialTheme.colorScheme.secondary)
                        }
                    }
                    if (!entry.disease.active) {
                        Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.errorContainer) {
                            Text(
                                stringResource(R.string.disease_resolved),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (index < group.entries.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 42.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = color.copy(alpha = 0.7f))
        Text("$count", style = MaterialTheme.typography.labelSmall, color = color)
    }
}
