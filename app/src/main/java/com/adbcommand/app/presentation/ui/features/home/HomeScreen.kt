package com.adbcommand.app.presentation.ui.features.home

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdbCommanderHome(
    viewModel: HomeViewModel = hiltViewModel(),
    onShowCommands: (ip: String, adbPort: String, pairingPort: String, code: String) -> Unit = { _, _, _, _ -> }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                navigationIcon = {
                    Text(
                        "ADB Commander",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                },
                title = {
                    Column{
                        ShizukuStatusCard(
                            state = viewModel.shizukuState.collectAsStateWithLifecycle().value,
                            onRequestPermission = viewModel::onEvent
                        )
                    }
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

            // ── Connection Details ────────────────────────────────────────────
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

                    // Show error if IP or ports couldn't be read
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

                    // Message shown when code can't be read automatically
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
                        // Generate button — tries to read pairing code from system
                        Button(
                            onClick  = { viewModel.onEvent(HomeEvent.GenerateCode) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape  = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor   = MaterialTheme.colorScheme.onPrimary
                            )
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
                                Spacer(Modifier.width(8.dp))
                                Text("Generate", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Scan QR — placeholder for camera scanner
                        FilledTonalButton(
                            onClick  = { /* TODO: open QR scanner screen */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Scan QR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Copy Commands ─────────────────────────────────────────────────
            // Only show when we have enough info to build valid commands
            AnimatedVisibility(
                visible = state.ip.isNotBlank() && state.pairingPort.isNotBlank()
            ) {
                CopyCommandsCard(
                    pairCommand    = state.pairCommand,
                    connectCommand = state.connectCommand
                )
            }

            // ── Connection status snackbar-style banner ───────────────────────
            AnimatedVisibility(visible = state.connectionStatus != null) {
                ConnectionStatusBanner(
                    status    = state.connectionStatus,
                    onDismiss = { viewModel.onEvent(HomeEvent.DismissStatus) }
                )
            }

            // ── Bottom actions ────────────────────────────────────────────────
            Spacer(modifier = Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick  = { viewModel.onEvent(HomeEvent.TestConnection) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
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
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Show ADB Commands for this device", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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