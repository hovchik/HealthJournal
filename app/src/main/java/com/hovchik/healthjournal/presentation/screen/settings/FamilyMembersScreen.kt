package com.hovchik.healthjournal.presentation.screen.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hovchik.healthjournal.HealthJournalApp
import com.hovchik.healthjournal.R
import com.hovchik.healthjournal.domain.model.FamilyMember
import com.hovchik.healthjournal.util.PredefinedData
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

    fun addMember(name: String, relationship: String, weight: String = "", height: String = "", knownDiseases: List<String> = emptyList()) {
        viewModelScope.launch {
            val colors = listOf(0xFF1B6B4D, 0xFF3D6373, 0xFF6B4D1B, 0xFF6B1B4D, 0xFF4D1B6B, 0xFF1B4D6B)
            val color = colors.random().toInt()
            repo.insertMember(FamilyMember(name = name, relationship = relationship, avatarColor = color, weight = weight, height = height, knownDiseases = knownDiseases))
        }
    }

    fun updateMember(member: FamilyMember) {
        viewModelScope.launch { repo.updateMember(member) }
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
    var editingMember by remember { mutableStateOf<FamilyMember?>(null) }

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
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_family_member))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with instructions
            item(key = "header") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Groups,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.select_member_first),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                }
            }

            // Self profile (id = 0) - prominent card
            item(key = "self_profile") {
                val isActive = activeProfileId == 0L
                ElevatedCard(
                    onClick = { viewModel.selectProfile(0) },
                    colors = if (isActive) CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) else CardDefaults.elevatedCardColors(),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = if (isActive) 6.dp else 2.dp
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
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .then(
                                    if (isActive) Modifier.border(3.dp, MaterialTheme.colorScheme.tertiary, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.rel_self),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.default_profile),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isActive) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
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
                        defaultElevation = if (isActive) 6.dp else 2.dp
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
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(member.avatarColor))
                                .then(
                                    if (isActive) Modifier.border(3.dp, MaterialTheme.colorScheme.tertiary, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                member.name.take(1).uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                member.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (member.relationship.isNotBlank()) {
                                Text(
                                    member.relationship,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (isActive) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { editingMember = member }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_family_member),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = { viewModel.deleteMember(member) }) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        FamilyMemberDialog(
            title = stringResource(R.string.add_family_member),
            initialName = "",
            initialRelationship = "",
            initialWeight = "",
            initialHeight = "",
            initialKnownDiseases = emptyList(),
            onDismiss = { showAddDialog = false },
            onConfirm = { name, rel, weight, height, diseases ->
                viewModel.addMember(name, rel, weight, height, diseases)
                showAddDialog = false
            }
        )
    }

    editingMember?.let { member ->
        FamilyMemberDialog(
            title = stringResource(R.string.edit_family_member),
            initialName = member.name,
            initialRelationship = member.relationship,
            initialWeight = member.weight,
            initialHeight = member.height,
            initialKnownDiseases = member.knownDiseases,
            onDismiss = { editingMember = null },
            onConfirm = { name, rel, weight, height, diseases ->
                viewModel.updateMember(member.copy(name = name, relationship = rel, weight = weight, height = height, knownDiseases = diseases))
                editingMember = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FamilyMemberDialog(
    title: String,
    initialName: String,
    initialRelationship: String,
    initialWeight: String,
    initialHeight: String,
    initialKnownDiseases: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, relationship: String, weight: String, height: String, knownDiseases: List<String>) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var weight by remember { mutableStateOf(initialWeight) }
    var height by remember { mutableStateOf(initialHeight) }
    var knownDiseases by remember { mutableStateOf(initialKnownDiseases) }
    var newDisease by remember { mutableStateOf("") }
    val otherLabel = stringResource(R.string.rel_other)

    // Determine initial chip state from initialRelationship
    val predefinedLabels = PredefinedData.relationships.map { stringResource(it) }
    val initialIsPredefined = initialRelationship in predefinedLabels
    var relationship by remember {
        mutableStateOf(
            if (initialIsPredefined) initialRelationship
            else if (initialRelationship.isNotBlank()) otherLabel
            else ""
        )
    }
    var customRelationship by remember {
        mutableStateOf(
            if (!initialIsPredefined && initialRelationship.isNotBlank()) initialRelationship else ""
        )
    }
    val isOtherSelected = relationship == otherLabel

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.PersonAdd, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        },
        title = {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.member_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
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

                HorizontalDivider()

                // Weight and Height
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text(stringResource(R.string.user_weight)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text(stringResource(R.string.user_height)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Known diseases
                Text(stringResource(R.string.known_diseases_title), style = MaterialTheme.typography.labelMedium)
                if (knownDiseases.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        knownDiseases.forEach { disease ->
                            InputChip(
                                selected = false,
                                onClick = { knownDiseases = knownDiseases - disease },
                                label = { Text(disease) },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.delete), modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newDisease,
                        onValueChange = { newDisease = it },
                        label = { Text(stringResource(R.string.add_known_disease)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    FilledTonalIconButton(
                        onClick = {
                            if (newDisease.isNotBlank()) {
                                knownDiseases = knownDiseases + newDisease.trim()
                                newDisease = ""
                            }
                        },
                        enabled = newDisease.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_known_disease))
                    }
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
                    onConfirm(name, rel, weight.trim(), height.trim(), knownDiseases)
                },
                enabled = name.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = MaterialTheme.shapes.medium) {
                Text(stringResource(R.string.close))
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}
