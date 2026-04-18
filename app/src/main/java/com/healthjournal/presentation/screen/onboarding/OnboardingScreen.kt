package com.healthjournal.presentation.screen.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val icon: ImageVector,
    val gradientStart: Color,
    val gradientEnd: Color,
    val titleResId: Int,
    val descResId: Int
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onOpenLegalDocument: (String) -> Unit = {},
    viewModel: OnboardingViewModel = viewModel()
) {
    val pages = listOf(
        OnboardingPageData(
            icon = Icons.Default.HealthAndSafety,
            gradientStart = Color(0xFF00796B),
            gradientEnd = Color(0xFF26A69A),
            titleResId = R.string.onboarding_welcome,
            descResId = R.string.onboarding_welcome_desc
        ),
        OnboardingPageData(
            icon = Icons.Default.Favorite,
            gradientStart = Color(0xFF1565C0),
            gradientEnd = Color(0xFF42A5F5),
            titleResId = R.string.onboarding_track_health,
            descResId = R.string.onboarding_track_health_desc
        ),
        OnboardingPageData(
            icon = Icons.Default.Psychology,
            gradientStart = Color(0xFF6A1B9A),
            gradientEnd = Color(0xFFAB47BC),
            titleResId = R.string.onboarding_setup,
            descResId = R.string.onboarding_welcome_desc // reuse until specific string added
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("") }
    var aiConsent by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    val currentPage = pagerState.currentPage
    val currentPageData = pages[currentPage]

    // Animated gradient background
    val bgStart by animateColorAsState(
        targetValue = currentPageData.gradientStart,
        animationSpec = tween(600),
        label = "bgStart"
    )
    val bgEnd by animateColorAsState(
        targetValue = currentPageData.gradientEnd,
        animationSpec = tween(600),
        label = "bgEnd"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgStart, bgEnd)))
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Pager ────────────────────────────────────────────────────────
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    pages.size - 1 -> AiConsentPage(
                        icon = pages[page].icon,
                        userName = userName,
                        onUserNameChange = { userName = it },
                        aiConsent = aiConsent,
                        onAiConsentChange = { aiConsent = it },
                        termsAccepted = termsAccepted,
                        onTermsAcceptedChange = { termsAccepted = it },
                        onOpenLegalDocument = onOpenLegalDocument
                    )
                    else -> StandardOnboardingPage(
                        icon = pages[page].icon,
                        title = stringResource(pages[page].titleResId),
                        description = stringResource(pages[page].descResId)
                    )
                }
            }

            // ── Bottom controls ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Page indicator dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.forEachIndexed { index, _ ->
                        PageDot(
                            selected = index == currentPage,
                            color = currentPageData.gradientStart
                        )
                        if (index < pages.size - 1) Spacer(Modifier.width(8.dp))
                    }
                }

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
                            },
                            shape = MaterialTheme.shapes.extraLarge,
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text(stringResource(R.string.back), fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Spacer(Modifier.width(80.dp))
                    }

                    if (currentPage < pages.size - 1) {
                        Button(
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentPageData.gradientStart
                            ),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_next),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.completeOnboarding(userName, aiConsent)
                                onComplete()
                            },
                            enabled = termsAccepted,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentPageData.gradientStart,
                                disabledContainerColor = currentPageData.gradientStart.copy(alpha = 0.4f),
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            ),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_start),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageDot(selected: Boolean, color: Color) {
    val width: Dp by animateDpAsState(
        targetValue = if (selected) 28.dp else 8.dp,
        animationSpec = tween(300),
        label = "dotWidth"
    )
    val alpha by animateColorAsState(
        targetValue = if (selected) color else color.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "dotColor"
    )
    Box(
        modifier = Modifier
            .height(8.dp)
            .width(width)
            .clip(CircleShape)
            .background(alpha)
    )
}

@Composable
private fun StandardOnboardingPage(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large icon in white circle
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.22f),
            modifier = Modifier.size(140.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.30f),
                    modifier = Modifier.size(110.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.80f),
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
    }
}

@Composable
private fun AiConsentPage(
    icon: ImageVector,
    userName: String,
    onUserNameChange: (String) -> Unit,
    aiConsent: Boolean,
    onAiConsentChange: (Boolean) -> Unit,
    termsAccepted: Boolean,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onOpenLegalDocument: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.22f),
            modifier = Modifier.size(110.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_setup),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            label = {
                Text(
                    stringResource(R.string.your_name),
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                cursorColor = Color.White
            ),
            shape = MaterialTheme.shapes.large
        )

        Spacer(Modifier.height(20.dp))

        Surface(
            shape = MaterialTheme.shapes.large,
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = aiConsent,
                    onCheckedChange = onAiConsentChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.White,
                        uncheckedColor = Color.White.copy(alpha = 0.6f),
                        checkmarkColor = Color(0xFF6A1B9A)
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ai_consent_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Terms + Privacy acceptance. The Start button is disabled until
        // this is checked. The links open the bundled docs in the legal
        // viewer so users can read them before accepting.
        Surface(
            shape = MaterialTheme.shapes.large,
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = onTermsAcceptedChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            uncheckedColor = Color.White.copy(alpha = 0.6f),
                            checkmarkColor = Color(0xFF6A1B9A)
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_terms_accept),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(
                        onClick = { onOpenLegalDocument("terms") },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            stringResource(R.string.legal_terms_of_use),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    }
                    TextButton(
                        onClick = { onOpenLegalDocument("privacy") },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            stringResource(R.string.legal_privacy_policy),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    }
                }
            }
        }
    }
}
