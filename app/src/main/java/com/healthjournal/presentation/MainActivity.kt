package com.healthjournal.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.healthjournal.presentation.navigation.HealthNavHost
import com.healthjournal.presentation.navigation.Screen
import com.healthjournal.presentation.navigation.bottomNavItems
import com.healthjournal.presentation.screen.onboarding.OnboardingViewModel
import com.healthjournal.presentation.screen.security.LockScreen
import com.healthjournal.presentation.screen.security.isAppLockEnabled
import com.healthjournal.presentation.theme.HealthJournalTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainApp()
        }
    }
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = viewModel()
    val settings by onboardingViewModel.settings.collectAsStateWithLifecycle()

    val startDestination = if (settings.onboardingCompleted) Screen.Home.route else Screen.Onboarding.route

    var isLocked by remember { mutableStateOf(isAppLockEnabled(context)) }

    HealthJournalTheme(themeMode = settings.themeMode) {
        if (isLocked) {
            LockScreen(onUnlocked = { isLocked = false })
        } else {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val hideBottomBarRoutes = setOf(
                Screen.Onboarding.route,
                Screen.ProfileSelection.route
            )
            val showBottomBar = currentRoute != null && currentRoute !in hideBottomBarRoutes

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    AnimatedVisibility(
                        visible = showBottomBar,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        NavigationBar(
                            tonalElevation = 3.dp,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            bottomNavItems.forEach { screen ->
                                val title = stringResource(screen.titleResId)
                                val selected = currentRoute == screen.route
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            screen.icon,
                                            contentDescription = title
                                        )
                                    },
                                    label = {
                                        Text(
                                            title,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    },
                                    selected = selected,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Home.route) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                HealthNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
