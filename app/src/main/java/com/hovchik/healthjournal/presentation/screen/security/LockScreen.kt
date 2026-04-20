package com.hovchik.healthjournal.presentation.screen.security

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.hovchik.healthjournal.R
import com.hovchik.healthjournal.util.BiometricHelper

@Composable
fun LockScreen(
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("security", Context.MODE_PRIVATE) }
    val biometricEnabled = prefs.getBoolean("biometric_lock", false)
    val pinEnabled = prefs.getBoolean("pin_lock", false)
    val savedPin = prefs.getString("pin_code", null)

    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var biometricAttempted by remember { mutableStateOf(false) }

    // Show biometric prompt on launch if enabled
    LaunchedEffect(Unit) {
        if (biometricEnabled && !biometricAttempted) {
            biometricAttempted = true
            val activity = context as? FragmentActivity
            if (activity != null && BiometricHelper.isBiometricAvailable(context)) {
                BiometricHelper.showBiometricPrompt(
                    activity = activity,
                    title = context.getString(R.string.biometric_unlock_title),
                    subtitle = context.getString(R.string.biometric_unlock_subtitle),
                    onSuccess = { onUnlocked() },
                    onError = { /* user can still use PIN */ },
                    onFailed = { /* user can still use PIN */ }
                )
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.app_locked_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                stringResource(R.string.app_locked_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN input if PIN lock is enabled
            if (pinEnabled && savedPin != null) {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                            pinInput = it
                            pinError = false
                        }
                    },
                    label = { Text(stringResource(R.string.enter_pin)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = pinError,
                    supportingText = if (pinError) {
                        { Text(stringResource(R.string.pin_incorrect)) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (pinInput == savedPin) {
                            onUnlocked()
                        } else {
                            pinError = true
                            pinInput = ""
                        }
                    },
                    enabled = pinInput.length >= 4,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(stringResource(R.string.unlock))
                }
            }

            // Biometric button (retry)
            if (biometricEnabled && BiometricHelper.isBiometricAvailable(context)) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            BiometricHelper.showBiometricPrompt(
                                activity = activity,
                                title = context.getString(R.string.biometric_unlock_title),
                                subtitle = context.getString(R.string.biometric_unlock_subtitle),
                                onSuccess = { onUnlocked() },
                                onError = {},
                                onFailed = {}
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.use_biometric))
                }
            }
        }
    }
}

fun isAppLockEnabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences("security", Context.MODE_PRIVATE)
    return prefs.getBoolean("biometric_lock", false) || prefs.getBoolean("pin_lock", false)
}
