package com.adbcommand.app.presentation.ui.features.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adbcommand.app.domain.models.UserPlan
import com.adbcommand.app.presentation.ui.features.home.ShizukuState
import com.android.billingclient.BuildConfig


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state       by viewModel.uiState.collectAsStateWithLifecycle()
    val shizuku     by viewModel.shizukuState.collectAsStateWithLifecycle()
    val entitlement by viewModel.entitlement.collectAsStateWithLifecycle()
    val context      = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.restoreMessage) {
        state.restoreMessage?.let {
            snackbarHost.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.onEvent(SettingsEvent.DismissMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Shizuku ───────────────────────────────────────────────────────
            SettingsSectionLabel("Shizuku")
            ShizukuCard(
                shizukuState        = shizuku,
                onRequestPermission = { viewModel.onEvent(SettingsEvent.RequestShizuku) }
            )

            // ── Account / Pro ─────────────────────────────────────────────────
            SettingsSectionLabel("Account")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Plan badge
                    ListItem(
                        headlineContent = { Text("Current Plan", fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            Text(
                                if (entitlement.plan == UserPlan.PRO) "Pro — all features unlocked"
                                else "Free — upgrade to unlock Pro features"
                            )
                        },
                        trailingContent = {
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = if (entitlement.plan == UserPlan.PRO)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    if (entitlement.plan == UserPlan.PRO) "PRO" else "FREE",
                                    style      = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color      = if (entitlement.plan == UserPlan.PRO)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(.1f))

                    if (entitlement.plan == UserPlan.FREE) {
                        SettingsItem(
                            icon    = Icons.Default.Lock,
                            label   = "Upgrade to Pro",
                            onClick = onNavigateToPaywall
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(.1f))
                    }

                    SettingsItem(
                        icon      = Icons.Default.Restore,
                        label     = "Restore Purchase",
                        isLoading = state.isRestoringPurchase,
                        onClick   = { viewModel.onEvent(SettingsEvent.RestorePurchase) }
                    )
                }
            }

            // ── About ─────────────────────────────────────────────────────────
            SettingsSectionLabel("About")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ListItem(
                        headlineContent   = { Text("Version", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(BuildConfig.VERSION_NAME) },
                        leadingContent    = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(.1f))

                    SettingsItem(
                        icon  = Icons.Default.Star,
                        label = "Rate on Play Store",
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=${context.packageName}")
                            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(.1f))

                    SettingsItem(
                        icon  = Icons.Default.PrivacyTip,
                        label = "Privacy Policy",
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                // Replace with your actual privacy policy URL
                                Uri.parse("https://your-site.com/privacy")
                            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(.1f))

                    SettingsItem(
                        icon  = Icons.Default.Email,
                        label = "Contact Support",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data    = Uri.parse("mailto:support@yourapp.com")
                                putExtra(Intent.EXTRA_SUBJECT, "ADB Commander Support")
                                flags   = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(.1f))

                    SettingsItem(
                        icon  = Icons.Default.Code,
                        label = "View on GitHub",
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/yourusername/adb-commander")
                            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Shizuku card ──────────────────────────────────────────────────────────────

@Composable
private fun ShizukuCard(
    shizukuState: ShizukuState,
    onRequestPermission: () -> Unit
) {
    val containerColor = when {
        shizukuState.isFullyAvailable -> MaterialTheme.colorScheme.primaryContainer
        shizukuState.isRunning -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when {
        shizukuState.isFullyAvailable -> MaterialTheme.colorScheme.onPrimaryContainer
        shizukuState.isRunning        -> MaterialTheme.colorScheme.onSecondaryContainer
        else  -> MaterialTheme.colorScheme.onErrorContainer
    }
    val statusText = when {
        shizukuState.isFullyAvailable -> "Active — privileged shell access enabled"
        shizukuState.isRunning  -> "Running but permission not granted"
        else  -> "Not running — start Shizuku to enable Pro features"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (shizukuState.isFullyAvailable) Icons.Default.CheckCircle
                else if (shizukuState.isRunning) Icons.Default.Lock
                else Icons.Default.ErrorOutline,
                null,
                Modifier.size(24.dp),
                tint = contentColor
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Shizuku",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = contentColor
                )
                Text(
                    statusText,
                    style     = MaterialTheme.typography.labelSmall,
                    color     = contentColor.copy(.8f),
                    lineHeight = 16.sp
                )
                if (shizukuState.isRunning && !shizukuState.isPermissionGranted) {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onRequestPermission,
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = contentColor,
                            contentColor   = containerColor
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier       = Modifier.height(34.dp)
                    ) {
                        Text("Grant Permission", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Reusable components ───────────────────────────────────────────────────────

@Composable
private fun SettingsSectionLabel(title: String) {
    Text(
        title,
        style    = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color    = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    label: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(label, fontWeight = FontWeight.SemiBold)
        },
        leadingContent = {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    Icons.Default.ChevronRight, null,
                    modifier = Modifier.size(18.dp),
                    tint     = MaterialTheme.colorScheme.outline
                )
            }
        },
        modifier = Modifier.padding(0.dp)
    )
    DisposableEffect(Unit) {
        onDispose { }
    }
    // Make the whole row tappable
    Surface(
        onClick = onClick,
        color   = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-56).dp)
            .height(56.dp)
    ) {}
}