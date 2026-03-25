package com.healthjournal.presentation.screen.security

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.healthjournal.R
import com.healthjournal.util.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("security", Context.MODE_PRIVATE) }
    var biometricEnabled by remember { mutableStateOf(prefs.getBoolean("biometric_lock", false)) }
    var pinEnabled by remember { mutableStateOf(prefs.getBoolean("pin_lock", false)) }
    var auditLogEnabled by remember { mutableStateOf(prefs.getBoolean("audit_log", true)) }
    val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showPinChangeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.security_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Biometric Lock
            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.biometric_lock_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                stringResource(R.string.biometric_lock_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { enabled ->
                                if (!biometricAvailable) return@Switch
                                if (enabled) {
                                    // Verify biometric before enabling
                                    val activity = context as? FragmentActivity
                                    if (activity != null) {
                                        BiometricHelper.showBiometricPrompt(
                                            activity = activity,
                                            title = context.getString(R.string.biometric_verify_title),
                                            subtitle = context.getString(R.string.biometric_verify_subtitle),
                                            onSuccess = {
                                                biometricEnabled = true
                                                prefs.edit().putBoolean("biometric_lock", true).apply()
                                            },
                                            onError = { /* don't enable */ },
                                            onFailed = { /* don't enable */ }
                                        )
                                    }
                                } else {
                                    biometricEnabled = false
                                    prefs.edit().putBoolean("biometric_lock", false).apply()
                                }
                            },
                            enabled = biometricAvailable
                        )
                    }
                    if (!biometricAvailable) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.biometric_not_available),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // PIN Lock
            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Pin, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.pin_lock_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                stringResource(R.string.pin_lock_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = pinEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPinSetupDialog = true
                                } else {
                                    pinEnabled = false
                                    prefs.edit()
                                        .putBoolean("pin_lock", false)
                                        .remove("pin_code")
                                        .apply()
                                }
                            }
                        )
                    }
                    if (pinEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showPinChangeDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.change_pin))
                        }
                    }
                }
            }

            // Audit Log
            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.History, contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.audit_log_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.audit_log_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = auditLogEnabled,
                        onCheckedChange = { enabled ->
                            auditLogEnabled = enabled
                            prefs.edit().putBoolean("audit_log", enabled).apply()
                        }
                    )
                }
            }

            // Security info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.security_info),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // PIN Setup Dialog
    if (showPinSetupDialog) {
        PinSetupDialog(
            onDismiss = { showPinSetupDialog = false },
            onPinSet = { pin ->
                prefs.edit()
                    .putString("pin_code", pin)
                    .putBoolean("pin_lock", true)
                    .apply()
                pinEnabled = true
                showPinSetupDialog = false
            }
        )
    }

    // PIN Change Dialog
    if (showPinChangeDialog) {
        PinChangeDialog(
            currentPin = prefs.getString("pin_code", "") ?: "",
            onDismiss = { showPinChangeDialog = false },
            onPinChanged = { newPin ->
                prefs.edit().putString("pin_code", newPin).apply()
                showPinChangeDialog = false
            }
        )
    }
}

@Composable
private fun PinSetupDialog(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Pin, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        },
        title = {
            Text(
                if (step == 1) stringResource(R.string.set_pin_title)
                else stringResource(R.string.confirm_pin_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    if (step == 1) stringResource(R.string.set_pin_desc)
                    else stringResource(R.string.confirm_pin_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = if (step == 1) pin else confirmPin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                            if (step == 1) pin = it else confirmPin = it
                            error = false
                        }
                    },
                    label = { Text(stringResource(R.string.pin_label)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = error,
                    supportingText = if (error) {
                        { Text(stringResource(R.string.pin_mismatch)) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        if (pin.length >= 4) {
                            step = 2
                        }
                    } else {
                        if (confirmPin == pin) {
                            onPinSet(pin)
                        } else {
                            error = true
                            confirmPin = ""
                        }
                    }
                },
                enabled = if (step == 1) pin.length >= 4 else confirmPin.length >= 4,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    if (step == 1) stringResource(R.string.next)
                    else stringResource(R.string.save)
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = MaterialTheme.shapes.medium) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Composable
private fun PinChangeDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onPinChanged: (String) -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) } // 1: verify old, 2: new pin, 3: confirm
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Pin, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        },
        title = {
            Text(
                when (step) {
                    1 -> stringResource(R.string.verify_current_pin)
                    2 -> stringResource(R.string.set_new_pin)
                    else -> stringResource(R.string.confirm_pin_title)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = when (step) {
                        1 -> oldPin
                        2 -> newPin
                        else -> confirmPin
                    },
                    onValueChange = {
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                            when (step) {
                                1 -> oldPin = it
                                2 -> newPin = it
                                else -> confirmPin = it
                            }
                            error = false
                        }
                    },
                    label = { Text(stringResource(R.string.pin_label)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = error,
                    supportingText = if (error) {
                        {
                            Text(
                                if (step == 1) stringResource(R.string.pin_incorrect)
                                else stringResource(R.string.pin_mismatch)
                            )
                        }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (step) {
                        1 -> {
                            if (oldPin == currentPin) {
                                step = 2
                            } else {
                                error = true
                                oldPin = ""
                            }
                        }
                        2 -> {
                            if (newPin.length >= 4) {
                                step = 3
                            }
                        }
                        else -> {
                            if (confirmPin == newPin) {
                                onPinChanged(newPin)
                            } else {
                                error = true
                                confirmPin = ""
                            }
                        }
                    }
                },
                enabled = when (step) {
                    1 -> oldPin.length >= 4
                    2 -> newPin.length >= 4
                    else -> confirmPin.length >= 4
                },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    if (step < 3) stringResource(R.string.next)
                    else stringResource(R.string.save)
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = MaterialTheme.shapes.medium) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}
