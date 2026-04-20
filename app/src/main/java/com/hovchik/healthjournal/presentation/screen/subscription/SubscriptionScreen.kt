package com.hovchik.healthjournal.presentation.screen.subscription

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hovchik.healthjournal.R
import com.hovchik.healthjournal.domain.model.subscription.SubscriptionPlan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val status by viewModel.subscriptionStatus.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.purchaseSuccess) {
        if (uiState.purchaseSuccess) {
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.subscription_title), fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current plan badge
            CurrentPlanBadge(planName = stringResource(planDisplayName(status.plan)))

            // Plan cards
            PlanCard(
                plan = SubscriptionPlan.FREE,
                isCurrentPlan = status.plan == SubscriptionPlan.FREE,
                isPurchasing = false,
                onSelect = {}
            )

            PlanCard(
                plan = SubscriptionPlan.BASIC,
                isCurrentPlan = status.plan == SubscriptionPlan.BASIC,
                isPurchasing = uiState.isPurchasing && uiState.selectedPlan == SubscriptionPlan.BASIC,
                onSelect = { viewModel.purchasePlan(SubscriptionPlan.BASIC) }
            )

            PlanCard(
                plan = SubscriptionPlan.PRO,
                isCurrentPlan = status.plan == SubscriptionPlan.PRO,
                isPurchasing = uiState.isPurchasing && uiState.selectedPlan == SubscriptionPlan.PRO,
                isRecommended = true,
                onSelect = { viewModel.purchasePlan(SubscriptionPlan.PRO) }
            )

            PlanCard(
                plan = SubscriptionPlan.UNLIMITED,
                isCurrentPlan = status.plan == SubscriptionPlan.UNLIMITED,
                isPurchasing = uiState.isPurchasing && uiState.selectedPlan == SubscriptionPlan.UNLIMITED,
                onSelect = { viewModel.purchasePlan(SubscriptionPlan.UNLIMITED) }
            )

            // Cloud AI usage info for Pro/Unlimited users
            if (status.plan.hasCloudAi) {
                CloudAiUsageCard(
                    used = status.cloudAiReportsUsedThisMonth,
                    limit = status.plan.cloudAiReportsPerMonth,
                    isUnlimited = status.plan.hasUnlimitedCloudAi
                )
            }

            // Error message
            uiState.errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }
            }

            // Token pricing explanation
            TokenPricingInfo()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CurrentPlanBadge(planName: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column {
                Text(
                    stringResource(R.string.subscription_current_plan),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    planName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isCurrentPlan: Boolean,
    isPurchasing: Boolean,
    isRecommended: Boolean = false,
    onSelect: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isCurrentPlan -> MaterialTheme.colorScheme.primary
            isRecommended -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "borderColor"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlan) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = if (isCurrentPlan || isRecommended) 2.dp else 1.dp,
            color = borderColor
        ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            planIcon(plan),
                            contentDescription = null,
                            tint = if (isRecommended) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            stringResource(planDisplayName(plan)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isRecommended) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                stringResource(R.string.subscription_recommended),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Price
                Column(horizontalAlignment = Alignment.End) {
                    if (plan.monthlyPriceUsd > 0) {
                        Text(
                            "$${String.format("%.2f", plan.monthlyPriceUsd)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(R.string.subscription_per_month),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            stringResource(R.string.subscription_free_price),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Features list
            val features = planFeatures(plan)
            features.forEach { (icon, textResId, included) ->
                FeatureRow(icon = icon, text = stringResource(textResId), included = included)
            }

            // Action button
            if (!isCurrentPlan && plan != SubscriptionPlan.FREE) {
                Button(
                    onClick = onSelect,
                    enabled = !isPurchasing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (isRecommended) ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ) else ButtonDefaults.buttonColors()
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        stringResource(R.string.subscription_subscribe),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (isCurrentPlan) {
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.subscription_current))
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, text: String, included: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            if (included) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (included) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = if (included) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = if (!included) TextDecoration.LineThrough else null
        )
    }
}

@Composable
private fun CloudAiUsageCard(used: Int, limit: Int, isUnlimited: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Cloud,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.subscription_cloud_ai_usage),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            if (isUnlimited) {
                Text(
                    stringResource(R.string.subscription_unlimited_reports, used),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            } else {
                Text(
                    stringResource(R.string.subscription_reports_used, used, limit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                LinearProgressIndicator(
                    progress = { (used.toFloat() / limit).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (used >= limit) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun TokenPricingInfo() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    stringResource(R.string.subscription_pricing_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                stringResource(R.string.subscription_pricing_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun planIcon(plan: SubscriptionPlan): ImageVector = when (plan) {
    SubscriptionPlan.FREE -> Icons.Default.VolunteerActivism
    SubscriptionPlan.BASIC -> Icons.Default.Favorite
    SubscriptionPlan.PRO -> Icons.Default.Bolt
    SubscriptionPlan.UNLIMITED -> Icons.Default.AllInclusive
}

private fun planDisplayName(plan: SubscriptionPlan): Int = when (plan) {
    SubscriptionPlan.FREE -> R.string.subscription_plan_free
    SubscriptionPlan.BASIC -> R.string.subscription_plan_basic
    SubscriptionPlan.PRO -> R.string.subscription_plan_pro
    SubscriptionPlan.UNLIMITED -> R.string.subscription_plan_unlimited
}

private data class FeatureInfo(val icon: ImageVector, val textResId: Int, val included: Boolean)

private fun planFeatures(plan: SubscriptionPlan): List<FeatureInfo> = when (plan) {
    SubscriptionPlan.FREE -> listOf(
        FeatureInfo(Icons.Default.MonitorHeart, R.string.subscription_feat_vitals, true),
        FeatureInfo(Icons.Default.Psychology, R.string.subscription_feat_local_ai, true),
        FeatureInfo(Icons.Default.Person, R.string.subscription_feat_one_family, true),
        FeatureInfo(Icons.Default.Cloud, R.string.subscription_feat_cloud_ai, false),
        FeatureInfo(Icons.Default.PictureAsPdf, R.string.subscription_feat_pdf, false),
        FeatureInfo(Icons.Default.FileDownload, R.string.subscription_feat_export, false)
    )
    SubscriptionPlan.BASIC -> listOf(
        FeatureInfo(Icons.Default.MonitorHeart, R.string.subscription_feat_vitals, true),
        FeatureInfo(Icons.Default.Psychology, R.string.subscription_feat_local_ai, true),
        FeatureInfo(Icons.Default.Groups, R.string.subscription_feat_unlimited_family, true),
        FeatureInfo(Icons.Default.PictureAsPdf, R.string.subscription_feat_pdf, true),
        FeatureInfo(Icons.Default.FileDownload, R.string.subscription_feat_export, true),
        FeatureInfo(Icons.Default.Cloud, R.string.subscription_feat_cloud_ai, false)
    )
    SubscriptionPlan.PRO -> listOf(
        FeatureInfo(Icons.Default.MonitorHeart, R.string.subscription_feat_vitals, true),
        FeatureInfo(Icons.Default.Psychology, R.string.subscription_feat_local_ai, true),
        FeatureInfo(Icons.Default.Groups, R.string.subscription_feat_unlimited_family, true),
        FeatureInfo(Icons.Default.PictureAsPdf, R.string.subscription_feat_pdf, true),
        FeatureInfo(Icons.Default.FileDownload, R.string.subscription_feat_export, true),
        FeatureInfo(Icons.Default.Cloud, R.string.subscription_feat_cloud_ai_30, true)
    )
    SubscriptionPlan.UNLIMITED -> listOf(
        FeatureInfo(Icons.Default.MonitorHeart, R.string.subscription_feat_vitals, true),
        FeatureInfo(Icons.Default.Psychology, R.string.subscription_feat_local_ai, true),
        FeatureInfo(Icons.Default.Groups, R.string.subscription_feat_unlimited_family, true),
        FeatureInfo(Icons.Default.PictureAsPdf, R.string.subscription_feat_pdf, true),
        FeatureInfo(Icons.Default.FileDownload, R.string.subscription_feat_export, true),
        FeatureInfo(Icons.Default.Cloud, R.string.subscription_feat_cloud_ai_unlimited, true)
    )
}
