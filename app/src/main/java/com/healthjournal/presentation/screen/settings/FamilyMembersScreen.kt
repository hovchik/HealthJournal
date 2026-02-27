package com.healthjournal.presentation.screen.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.HealthJournalApp
import com.healthjournal.R
import com.healthjournal.domain.model.FamilyMember
import com.healthjournal.util.PredefinedData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FamilyMembersViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container
    private val repo = container.familyMemberRepository
    private val settingsRepo = container.userSettingsRepository

    val members = repo.getAllMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfileId = settingsRepo.getUserSettings()
        .map { it.activeProfileId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun addMember(name: String, relationship: String) {
        viewModelScope.launch {
            val colors = listOf(0xFF1B6B4D, 0xFF3D6373, 0xFF6B4D1B, 0xFF6B1B4D, 0xFF4D1B6B, 0xFF1B4D6B)
            val color = colors.random().toInt()
            repo.insertMember(FamilyMember(name = name, relationship = relationship, avatarColor = color))
        }
    }

    fun deleteMember(member: FamilyMember) {
        viewModelScope.launch { repo.deleteMember(member) }
    }

    fun selectProfile(profileId: Long) {
        viewModelScope.launch { settingsRepo.setActiveProfileId(profileId) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
    onBack: () -> Unit,
    viewModel: FamilyMembersViewModel = viewModel()
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.family_members_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_family_member))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Self profile (id = 0)
            item {
                val isActive = activeProfileId == 0L
                ElevatedCard(
                    onClick = { viewModel.selectProfile(0) },
                    colors = if (isActive) CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) else CardDefaults.elevatedCardColors(),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = if (isActive) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.rel_self), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.default_profile), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isActive) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            items(members, key = { it.id }) { member ->
                val isActive = activeProfileId == member.id
                ElevatedCard(
                    onClick = { viewModel.selectProfile(member.id) },
                    colors = if (isActive) CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) else CardDefaults.elevatedCardColors(),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = if (isActive) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(member.avatarColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                member.name.take(1).uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            if (member.relationship.isNotBlank()) {
                                Text(member.relationship, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (isActive) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.deleteMember(member) }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFamilyMemberDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, rel ->
                viewModel.addMember(name, rel)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddFamilyMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, relationship: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var customRelationship by remember { mutableStateOf("") }
    val otherLabel = stringResource(R.string.rel_other)
    val isOtherSelected = relationship == otherLabel

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_family_member)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.member_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(stringResource(R.string.relationship), style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PredefinedData.relationships.forEach { resId ->
                        val label = stringResource(resId)
                        FilterChip(
                            selected = relationship == label,
                            onClick = {
                                relationship = label
                                if (label != otherLabel) customRelationship = ""
                            },
                            label = { Text(label) }
                        )
                    }
                }
                if (isOtherSelected) {
                    OutlinedTextField(
                        value = customRelationship,
                        onValueChange = { customRelationship = it },
                        label = { Text(stringResource(R.string.custom_relationship_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rel = if (isOtherSelected && customRelationship.isNotBlank()) {
                        customRelationship.trim()
                    } else {
                        relationship
                    }
                    onAdd(name, rel)
                },
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}
