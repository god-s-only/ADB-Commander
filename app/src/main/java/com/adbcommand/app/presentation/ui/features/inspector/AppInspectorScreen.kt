package com.adbcommand.app.presentation.ui.features.inspector

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adbcommand.app.domain.models.AppComponent
import com.adbcommand.app.domain.models.AppInspection
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInspectorScreen(
    packageName: String,
    onNavigateBack: () -> Unit,
    viewModel: AppInspectorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(packageName) {
        viewModel.onEvent(InspectorEvent.Load(packageName))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "App Inspector",
                            fontWeight    = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        if (state.inspection != null) {
                            Text(
                                state.inspection!!.appName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
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
        }
    ) { padding ->

        AnimatedContent(
            targetState  = state.isLoading,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label        = "inspector_content",
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) { loading ->

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            "Reading package info…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else if (state.error != null) {
                Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ErrorOutline, null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                state.error ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                state.inspection?.let { inspection ->
                    InspectionContent(
                        inspection  = inspection,
                        expandedSection = state.expandedSection,
                        onToggle = { viewModel.onEvent(InspectorEvent.ToggleSection(it)) }
                    )
                }
            }
        }
    }
}


@Composable
private fun InspectionContent(
    inspection: AppInspection,
    expandedSection: InspectorSection?,
    onToggle: (InspectorSection) -> Unit
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(
            start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        item {
            AppHeaderCard(inspection = inspection)
        }

        item {
            AccordionSection(
                section   = InspectorSection.IDENTITY,
                isExpanded = expandedSection == InspectorSection.IDENTITY,
                onToggle  = onToggle,
                badgeCount = null
            ) {
                InfoRow("Package",     inspection.packageName)
                InfoRow("Version",     "${inspection.versionName} (${inspection.versionCode})")
                InfoRow("Installed",   inspection.installedAt?.let { formatDate(it) })
                InfoRow("Updated",     inspection.updatedAt?.let { formatDate(it) })
                InfoRow("Installed via", friendlyInstaller(inspection.installerPackage))
            }
        }

        item {
            AccordionSection(
                section   = InspectorSection.BUILD,
                isExpanded = expandedSection == InspectorSection.BUILD,
                onToggle  = onToggle,
                badgeCount = null
            ) {
                InfoRow("Target SDK",  inspection.targetSdk?.let { "API $it (${sdkName(it)})" })
                InfoRow("Min SDK",     inspection.minSdk?.let { "API $it (${sdkName(it)})" })
                InfoRow("Compile SDK", inspection.compileSdk?.let { "API $it (${sdkName(it)})" })
                BadgeRow("Debuggable",  inspection.isDebuggable, trueColor = MaterialTheme.colorScheme.error)
                BadgeRow("Test Only",   inspection.isTestOnly,   trueColor = MaterialTheme.colorScheme.error)
            }
        }

        item {
            AccordionSection(
                section   = InspectorSection.STORAGE,
                isExpanded = expandedSection == InspectorSection.STORAGE,
                onToggle  = onToggle,
                badgeCount = null
            ) {
                InfoRow("APK Size",    inspection.apkSizeBytes?.let { formatBytes(it) })
                InfoRow("APK Path",    inspection.apkPath, monospace = true)
                InfoRow("Data Dir",    inspection.dataDir,  monospace = true)
            }
        }

        item {
            AccordionSection(
                section   = InspectorSection.SIGNING,
                isExpanded = expandedSection == InspectorSection.SIGNING,
                onToggle  = onToggle,
                badgeCount = null
            ) {
                InfoRow("Subject",    inspection.signingCertSubject)
                InfoRow("SHA-256",    inspection.signingCertSha256, monospace = true)
            }
        }

        item {
            AccordionSection(
                section    = InspectorSection.ACTIVITIES,
                isExpanded = expandedSection == InspectorSection.ACTIVITIES,
                onToggle   = onToggle,
                badgeCount = inspection.activities.size
            ) {
                if (inspection.activities.isEmpty()) {
                    EmptyComponentNote("No activities declared")
                } else {
                    inspection.activities.forEach { ComponentRow(it) }
                }
            }
        }

        item {
            AccordionSection(
                section    = InspectorSection.SERVICES,
                isExpanded = expandedSection == InspectorSection.SERVICES,
                onToggle   = onToggle,
                badgeCount = inspection.services.size
            ) {
                if (inspection.services.isEmpty()) {
                    EmptyComponentNote("No services declared")
                } else {
                    inspection.services.forEach { ComponentRow(it) }
                }
            }
        }

        item {
            AccordionSection(
                section    = InspectorSection.RECEIVERS,
                isExpanded = expandedSection == InspectorSection.RECEIVERS,
                onToggle   = onToggle,
                badgeCount = inspection.receivers.size
            ) {
                if (inspection.receivers.isEmpty()) {
                    EmptyComponentNote("No broadcast receivers declared")
                } else {
                    inspection.receivers.forEach { ComponentRow(it) }
                }
            }
        }

        item {
            AccordionSection(
                section    = InspectorSection.PROVIDERS,
                isExpanded = expandedSection == InspectorSection.PROVIDERS,
                onToggle   = onToggle,
                badgeCount = inspection.providers.size
            ) {
                if (inspection.providers.isEmpty()) {
                    EmptyComponentNote("No content providers declared")
                } else {
                    inspection.providers.forEach { ComponentRow(it) }
                }
            }
        }

        item {
            AccordionSection(
                section    = InspectorSection.PERMISSIONS,
                isExpanded = expandedSection == InspectorSection.PERMISSIONS,
                onToggle   = onToggle,
                badgeCount = inspection.requestedPermissions.size
            ) {
                if (inspection.requestedPermissions.isEmpty()) {
                    EmptyComponentNote("No permissions requested")
                } else {
                    if (inspection.grantedPermissions.isNotEmpty()) {
                        PermissionGroupLabel("Granted (${inspection.grantedPermissions.size})", granted = true)
                        inspection.grantedPermissions.forEach { PermissionRow(it, granted = true) }
                    }
                    if (inspection.deniedPermissions.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        PermissionGroupLabel("Denied (${inspection.deniedPermissions.size})", granted = false)
                        inspection.deniedPermissions.forEach { PermissionRow(it, granted = false) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppHeaderCard(inspection: AppInspection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            if (inspection.icon != null) {
                val bmp = remember(inspection.icon) {
                    inspection.icon.toBitmap().asImageBitmap()
                }
                Image(
                    bitmap             = bmp,
                    contentDescription = null,
                    modifier           = Modifier.size(56.dp)
                )
            } else {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Android, null, Modifier.size(28.dp))
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    inspection.appName,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    inspection.packageName,
                    style      = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color      = MaterialTheme.colorScheme.outline,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (inspection.isDebuggable) {
                        MiniChip("DEBUG", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                    }
                    inspection.targetSdk?.let {
                        MiniChip("API $it", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    MiniChip("v${inspection.versionName}", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}


@Composable
private fun AccordionSection(
    section: InspectorSection,
    isExpanded: Boolean,
    onToggle: (InspectorSection) -> Unit,
    badgeCount: Int?,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Surface(
            onClick = { onToggle(section) },
            color = androidx.compose.ui.graphics.Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    section.label,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f)
                )
                if (badgeCount != null && badgeCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            badgeCount.toString(),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier            = Modifier.padding(
                    start = 16.dp, end = 16.dp, bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content             = content
            )
        }
    }
}



@Composable
private fun InfoRow(
    label: String,
    value: String?,
    monospace: Boolean = false
) {
    val clipboard = LocalClipboardManager.current

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(100.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text       = value ?: "—",
            style      = MaterialTheme.typography.bodySmall,
            fontFamily = if (monospace) FontFamily.Monospace else null,
            color      = if (value != null) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.outline,
            modifier   = Modifier
                .weight(1f)
                .then(
                    if (value != null) Modifier else Modifier
                )
        )
        if (value != null) {
            IconButton(
                onClick  = { clipboard.setText(AnnotatedString(value)) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy, "Copy",
                    Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun BadgeRow(
    label: String,
    value: Boolean,
    trueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(100.dp)
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (value) trueColor.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text  = if (value) "Yes" else "No",
                style = MaterialTheme.typography.labelSmall,
                color = if (value) trueColor
                else MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun ComponentRow(component: AppComponent) {
    val clipboard = LocalClipboardManager.current

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                component.shortName,
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                component.name,
                style      = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color      = MaterialTheme.colorScheme.outline,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(6.dp))
        if (component.isExported) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    "exported",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 9.sp
                )
            }
            Spacer(Modifier.width(4.dp))
        }
        IconButton(
            onClick  = { clipboard.setText(AnnotatedString(component.name)) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.ContentCopy, "Copy",
                Modifier.size(13.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}


@Composable
private fun PermissionGroupLabel(label: String, granted: Boolean) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = if (granted) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.error
    )
}

@Composable
private fun PermissionRow(permission: String, granted: Boolean) {
    val clipboard = LocalClipboardManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (granted) Icons.Default.CheckCircle
            else Icons.Default.Cancel,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint     = if (granted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.width(8.dp))
        Text(
            permission.removePrefix("android.permission.").removePrefix("android."),
            style      = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier   = Modifier.weight(1f),
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis
        )
        IconButton(
            onClick  = { clipboard.setText(AnnotatedString(permission)) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.ContentCopy, "Copy", Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun EmptyComponentNote(message: String) {
    Text(
        message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
private fun MiniChip(
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Surface(shape = RoundedCornerShape(4.dp), color = containerColor) {
        Text(
            label,
            style    = MaterialTheme.typography.labelSmall,
            color    = contentColor,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatDate(ms: Long): String =
    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(ms))

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024         -> "${bytes}B"
    bytes < 1024 * 1024  -> "${"%.1f".format(bytes / 1024.0)}KB"
    else                 -> "${"%.1f".format(bytes / (1024.0 * 1024))}MB"
}

private fun friendlyInstaller(pkg: String?): String = when (pkg) {
    "com.android.vending"         -> "Google Play Store"
    "com.amazon.venezia"          -> "Amazon Appstore"
    "com.samsung.android.app.omcagent", "com.sec.android.app.samsungapps" -> "Samsung Galaxy Store"
    "org.fdroid.fdroid"           -> "F-Droid"
    null                          -> "Unknown / Sideloaded"
    else                          -> pkg
}

private fun sdkName(api: Int): String = when (api) {
    34   -> "Android 14"
    33   -> "Android 13"
    32, 31 -> "Android 12"
    30   -> "Android 11"
    29   -> "Android 10"
    28   -> "Android 9 Pie"
    27, 26 -> "Android 8 Oreo"
    25, 24 -> "Android 7 Nougat"
    23   -> "Android 6 Marshmallow"
    22, 21 -> "Android 5 Lollipop"
    else -> "API $api"
}