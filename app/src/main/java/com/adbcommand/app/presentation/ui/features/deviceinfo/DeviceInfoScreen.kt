package com.adbcommand.app.presentation.ui.features.deviceinfo

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adbcommand.app.domain.models.DeviceInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(
    onNavigateBack: () -> Unit,
    viewModel: DeviceInfoViewModel = hiltViewModel()
) {
    val state     by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard  = LocalClipboardManager.current
    val scope      = rememberCoroutineScope()

    LaunchedEffect(state.profileCopied) {
        if (state.profileCopied) {
            state.deviceInfo?.let { info ->
                clipboard.setText(AnnotatedString(info.toProfileString()))
            }
            delay(2000)
            viewModel.onEvent(DeviceInfoEvent.ClearCopiedStatus)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Device Info",
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(DeviceInfoEvent.Refresh) }) {
                        if (state.isLoading) {
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
    ) { padding ->

        AnimatedContent(
            targetState  = state.isLoading && state.deviceInfo == null,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label        = "device_info_content",
            modifier     = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { isFirstLoad ->

            if (isFirstLoad) {
                LoadingSkeleton()
            } else {
                Column(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    AnimatedVisibility(visible = state.error != null) {
                        state.error?.let { ErrorBanner(message = it) }
                    }

                    val info = state.deviceInfo

                    SectionLabel("System")

                    InfoCard {
                        InfoRow("Model",          info?.model,          Icons.Default.PhoneAndroid)
                        InfoRow("Manufacturer",   info?.manufacturer,   Icons.Default.Business)
                        InfoRow("Android",        info?.androidVersion, Icons.Default.Android)
                        InfoRow("API Level",      info?.apiLevel,       Icons.Default.Code)
                        InfoRow("Build",          info?.buildNumber,    Icons.Default.Build)
                        InfoRow("Security Patch", info?.securityPatch,  Icons.Default.Security)
                    }

                    SectionLabel("Hardware")

                    InfoCard {
                        InfoRow("Screen Size",    info?.screenSize,     Icons.Default.AspectRatio)
                        InfoRow("Density",        info?.screenDensity,  Icons.Default.Tune)
                        InfoRow("CPU ABI",        info?.cpuAbi,         Icons.Default.Memory)
                        InfoRow("Total RAM",      info?.totalRam,       Icons.Default.Storage)
                    }

                    SectionLabel("Battery")

                    info?.batteryLevel?.toIntOrNull()?.let { level ->
                        BatteryCard(level = level, status = info.batteryStatus)
                    }

                    InfoCard {
                        InfoRow("Health",      info?.batteryHealth,  Icons.Default.FavoriteBorder)
                        InfoRow("Temperature", info?.batteryTemp,    Icons.Default.Thermostat)
                        InfoRow("Voltage",     info?.batteryVoltage, Icons.Default.ElectricBolt)
                    }

                    SectionLabel("Network")

                    InfoCard {
                        InfoRow("IP Address", info?.ipAddress, Icons.Default.SettingsEthernet)
                        InfoRow("Wi-Fi",      info?.wifiState, Icons.Default.Wifi)
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick  = { viewModel.onEvent(DeviceInfoEvent.CopyProfile) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape    = RoundedCornerShape(16.dp),
                        enabled  = info != null
                    ) {
                        AnimatedContent(
                            targetState = state.profileCopied,
                            label       = "copy_button_label"
                        ) { copied ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (copied) Icons.Default.Check
                                    else Icons.Default.ContentCopy,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text       = if (copied) "Profile Copied!"
                                    else "Copy Device Profile",
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun BatteryCard(level: Int, status: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.BatteryFull,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint     = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Battery Level",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text       = "$level%",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = when {
                        level >= 50 -> MaterialTheme.colorScheme.primary
                        level >= 20 -> MaterialTheme.colorScheme.tertiary
                        else        -> MaterialTheme.colorScheme.error
                    }
                )
            }

            LinearProgressIndicator(
                progress        = { level / 100f },
                modifier        = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color           = when {
                    level >= 50 -> MaterialTheme.colorScheme.primary
                    level >= 20 -> MaterialTheme.colorScheme.tertiary
                    else        -> MaterialTheme.colorScheme.error
                },
                trackColor      = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap       = androidx.compose.ui.graphics.StrokeCap.Round
            )

            if (!status.isNullOrBlank()) {
                Text(
                    text  = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content             = content
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String?,
    icon: ImageVector
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint     = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text       = value ?: "—",
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = if (value != null)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.titleMedium,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun ErrorBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text  = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun LoadingSkeleton() {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        repeat(4) {
            ShimmerBox(width = 80.dp, height = 16.dp)
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
                    repeat(3) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ShimmerBox(width = 18.dp, height = 18.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ShimmerBox(width = 60.dp,  height = 10.dp)
                                ShimmerBox(width = 120.dp, height = 14.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerBox(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp
) {
    Surface(
        modifier = Modifier.size(width = width, height = height),
        shape    = RoundedCornerShape(4.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {}
}