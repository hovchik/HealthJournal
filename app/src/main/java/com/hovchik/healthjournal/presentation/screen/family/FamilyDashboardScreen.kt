package com.hovchik.healthjournal.presentation.screen.family

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hovchik.healthjournal.HealthJournalApp
import com.hovchik.healthjournal.R
import com.hovchik.healthjournal.domain.model.FamilyMember
import kotlinx.coroutines.flow.*

data class FamilyMemberSummary(
    val member: FamilyMember,
    val symptomCount: Int = 0,
    val vitalCount: Int = 0,
    val medicationCount: Int = 0,
    val diseaseCount: Int = 0
)

class FamilyDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    val familySummaries = combine(
        container.familyMemberRepository.getAllMembers(),
        container.symptomRepository.getAllSymptoms(),
        container.vitalSignRepository.getAllVitalSigns(),
        container.medicationRepository.getAllMedications(),
        container.diseaseRepository.getAllDiseases()
    ) { members, symptoms, vitals, meds, diseases ->
        // Add "self" as profile 0
        val selfSummary = FamilyMemberSummary(
            member = FamilyMember(id = 0, name = "Self"),
            symptomCount = symptoms.count { it.profileId == 0L },
            vitalCount = vitals.count { it.profileId == 0L },
            medicationCount = meds.count { it.profileId == 0L },
            diseaseCount = diseases.count { it.profileId == 0L && !it.isDeleted }
        )

        val memberSummaries = members.map { member ->
            FamilyMemberSummary(
                member = member,
                symptomCount = symptoms.count { it.profileId == member.id },
                vitalCount = vitals.count { it.profileId == member.id },
                medicationCount = meds.count { it.profileId == member.id },
                diseaseCount = diseases.count { it.profileId == member.id && !it.isDeleted }
            )
        }

        listOf(selfSummary) + memberSummaries
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDashboardScreen(
    onBack: () -> Unit,
    viewModel: FamilyDashboardViewModel = viewModel()
) {
    val summaries by viewModel.familySummaries.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.family_dashboard_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(summaries) { summary ->
                FamilyMemberCard(summary)
            }
        }
    }
}

@Composable
private fun FamilyMemberCard(summary: FamilyMemberSummary) {
    val avatarColor = Color(summary.member.avatarColor)

    ElevatedCard(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = avatarColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            summary.member.name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = avatarColor
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(summary.member.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (summary.member.relationship.isNotBlank()) {
                        Text(summary.member.relationship, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip(Icons.Default.Sick, "${summary.symptomCount}", stringResource(R.string.symptoms_title))
                StatChip(Icons.Default.MonitorHeart, "${summary.vitalCount}", stringResource(R.string.nav_vitals))
                StatChip(Icons.Default.Medication, "${summary.medicationCount}", stringResource(R.string.nav_medications))
                StatChip(Icons.Default.LocalHospital, "${summary.diseaseCount}", stringResource(R.string.disease_filter))
            }
        }
    }
}

@Composable
private fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
