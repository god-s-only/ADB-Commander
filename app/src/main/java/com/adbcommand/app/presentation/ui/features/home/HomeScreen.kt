package com.adbcommand.app.presentation.ui.features.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdbCommanderHome(
    viewModel: HomeViewModel = hiltViewModel(),
    onShowCommands: (ip: String, adbPort: String, pairingPort: String, code: String) -> Unit = { _, _, _, _ -> },
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCapture: () -> Unit = {},
    onNavigateToProcessMonitor: () -> Unit = {},
    onNavigateToIntentSender: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ADB Commander",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(HomeEvent.LoadInfo) }) {
                        if (state.isLoadingInfo) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) {
        if (state.qrData != null) {
            QrCodeDialog(
                data = state.qrData!!,
                pairingCode = state.pairingCode,
                onDismiss = { viewModel.onEvent(HomeEvent.DismissQr) }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 100.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            ShizukuStatusCard(
                state = viewModel.shizukuState.collectAsStateWithLifecycle().value,
                onRequestPermission = viewModel::onEvent
            )

            // ── Connection Details ────────────────────────────────────────────
            Text(
                text  = "Connection Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdbField(
                        label = "Device IP",
                        value = state.ip.ifBlank { "—" },
                        icon  = Icons.Default.SettingsEthernet
                    )
                    AdbField(
                        label = "ADB Port",
                        value = state.adbPort.ifBlank { "5555" },
                        icon  = Icons.Default.Code
                    )
                    AdbField(
                        label = "Pairing Port",
                        value = state.pairingPort.ifBlank { "—" },
                        icon  = Icons.Default.Router
                    )

                    AnimatedVisibility(
                        visible = state.infoError != null,
                        enter   = fadeIn(),
                        exit    = fadeOut()
                    ) {
                        state.infoError?.let { err ->
                            Text(
                                text  = err,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // ── Pairing Code ──────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdbField(
                        label = "Pairing Code",
                        value = state.pairingCode.ifBlank { "— — — —" },
                        icon  = Icons.Default.Numbers
                    )

                    AnimatedVisibility(visible = state.codeMessage != null) {
                        state.codeMessage?.let { msg ->
                            Text(
                                text  = msg,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick  = { viewModel.onEvent(HomeEvent.GenerateCode) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape  = RoundedCornerShape(12.dp)
                        ) {
                            if (state.isGeneratingCode) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color       = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    Icons.Default.AutoFixHigh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Generate", fontWeight = FontWeight.Bold)
                            }
                        }

                        FilledTonalButton(
                            onClick = { viewModel.onEvent(HomeEvent.GenerateCode) },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Show QR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Copy Commands ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = state.ip.isNotBlank() && state.pairingPort.isNotBlank()
            ) {
                CopyCommandsCard(
                    pairCommand    = state.pairCommand,
                    connectCommand = state.connectCommand
                )
            }

            // ── Connection status banner ──────────────────────────────────────
            AnimatedVisibility(visible = state.connectionStatus != null) {
                ConnectionStatusBanner(
                    status    = state.connectionStatus,
                    onDismiss = { viewModel.onEvent(HomeEvent.DismissStatus) }
                )
            }

            // ── Tools ─────────────────────────────────────────────────────────
            Text(
                text  = "Tools",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Icon-only tool buttons — no text wrapping possible
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolButton(
                    icon    = Icons.Default.Screenshot,
                    label   = "Capture",
                    onClick = onNavigateToCapture,
                    modifier = Modifier.weight(1f)
                )
                ToolButton(
                    icon    = Icons.Default.Memory,
                    label   = "Processes",
                    onClick = onNavigateToProcessMonitor,
                    modifier = Modifier.weight(1f)
                )
                ToolButton(
                    icon    = Icons.Default.Send,
                    label   = "Intents",
                    onClick = onNavigateToIntentSender,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Primary actions ───────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick  = { viewModel.onEvent(HomeEvent.TestConnection) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state.isTestingConnection) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Testing…", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CastConnected, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text("Test Connection", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                FilledTonalButton(
                    onClick  = {
                        onShowCommands(
                            state.ip,
                            state.adbPort,
                            state.pairingPort,
                            state.pairingCode
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Show ADB Commands",
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick  = onClick,
        modifier = modifier.height(64.dp),
        shape    = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text     = label,
                fontSize = 11.sp,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Copy Commands card ────────────────────────────────────────────────────────

@Composable
private fun CopyCommandsCard(
    pairCommand: String,
    connectCommand: String
) {
    val clipboard = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text  = "Quick Copy",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            CopyCommandRow(
                label   = "Pair",
                command = pairCommand,
                icon    = Icons.Default.Link,
                onCopy  = { clipboard.setText(AnnotatedString(pairCommand)) }
            )

            CopyCommandRow(
                label   = "Connect",
                command = connectCommand,
                icon    = Icons.Default.CastConnected,
                onCopy  = { clipboard.setText(AnnotatedString(connectCommand)) }
            )
        }
    }
}

@Composable
private fun CopyCommandRow(
    label: String,
    command: String,
    icon: ImageVector,
    onCopy: () -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint     = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text  = command,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onCopy) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Copy $label command",
                modifier = Modifier.size(18.dp),
                tint     = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ── Connection status banner ──────────────────────────────────────────────────

@Composable
private fun ConnectionStatusBanner(
    status: ConnectionStatus?,
    onDismiss: () -> Unit
) {
    val isSuccess = status == ConnectionStatus.SUCCESS

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (isSuccess)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = if (isSuccess) Icons.Default.CheckCircle
                else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint               = if (isSuccess)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text     = if (isSuccess) "Network reachable — device is online"
                else "Cannot reach network — check Wi-Fi",
                style    = MaterialTheme.typography.bodySmall,
                color    = if (isSuccess)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp),
                    tint     = if (isSuccess)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// ── Reusable field row ────────────────────────────────────────────────────────

@Composable
fun AdbField(label: String, value: String, icon: ImageVector) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint     = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    value,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun Default() {
    MaterialTheme {
        // Preview with fake state — no ViewModel needed
        AdbCommanderHomePreview()
    }
}

@Composable
private fun AdbCommanderHomePreview() {
    // Mirrors the real screen but with hardcoded state for the preview
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            LargeTopAppBar(
                title = {
                    Text(
                        "ADB Commander",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text     = "Connection Details",
                style    = MaterialTheme.typography.titleMedium,
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdbField("Device IP", "192.168.1.100", Icons.Default.SettingsEthernet)
                    AdbField("ADB Port", "5555", Icons.Default.Code)
                    AdbField("Pairing Port", "37057", Icons.Default.Router)
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdbField("Pairing Code", "— — — —", Icons.Default.Numbers)
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick  = {},
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Generate", fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick  = {},
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Scan QR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick  = {},
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape    = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CastConnected, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Test Connection", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                FilledTonalButton(
                    onClick  = {},
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape    = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Terminal, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Show ADB Commands for this device", fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
