package com.adbcommand.app.domain.usecase.process

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adbcommand.app.domain.models.ProcessInfo
import com.adbcommand.app.presentation.ui.features.processmonitor.ProcessMonitorEvent
import com.adbcommand.app.presentation.ui.features.processmonitor.ProcessMonitorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessMonitorScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProcessMonitorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val displayed = state.processes
        .filter { p ->
            (!state.showUserOnly || p.isUserApp) &&
                    (state.searchQuery.isBlank() ||
                            p.name.contains(state.searchQuery, ignoreCase = true))
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Process Monitor", fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
                        Text("${displayed.size} processes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(ProcessMonitorEvent.ToggleUserOnly) }) {
                        Icon(
                            if (state.showUserOnly) Icons.Default.PersonOff else Icons.Default.Person,
                            contentDescription = "Toggle user apps only",
                            tint = if (state.showUserOnly) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            OutlinedTextField(
                value         = state.searchQuery,
                onValueChange = { viewModel.onEvent(ProcessMonitorEvent.SearchChanged(it)) },
                placeholder   = { Text("Search processes…") },
                leadingIcon   = { Icon(Icons.Default.Search, null, Modifier.size(20.dp)) },
                trailingIcon  = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onEvent(ProcessMonitorEvent.SearchChanged("")) }) {
                            Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                        }
                    }
                },
                singleLine    = true,
                shape         = RoundedCornerShape(16.dp),
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PROCESS", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f))
                Text("CPU", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.width(50.dp))
                Text("RAM", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.width(60.dp))
            }

            if (!state.isRunning && state.processes.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Memory, null, Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(8.dp))
                        Text("Tap Start to monitor processes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(displayed, key = { it.pid }) { process ->
                        ProcessRow(process)
                    }
                }
            }

            Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.isRunning) {
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Monitoring…", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = { viewModel.onEvent(ProcessMonitorEvent.Stop) },
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Stop, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Stop", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick  = { viewModel.onEvent(ProcessMonitorEvent.Start) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Start Monitoring", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessRow(process: ProcessInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    process.name.substringAfterLast(":"),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    "PID ${process.pid}",
                    style      = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color      = MaterialTheme.colorScheme.outline
                )
            }

            Column(
                modifier          = Modifier.width(50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "${"%.1f".format(process.cpuPercent)}%",
                    style      = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color      = when {
                        process.cpuPercent > 50f -> MaterialTheme.colorScheme.error
                        process.cpuPercent > 20f -> MaterialTheme.colorScheme.tertiary
                        else                     -> MaterialTheme.colorScheme.primary
                    }
                )
                LinearProgressIndicator(
                    progress    = { process.cpuPercent / 100f },
                    modifier    = Modifier.fillMaxWidth().height(3.dp),
                    color       = when {
                        process.cpuPercent > 50f -> MaterialTheme.colorScheme.error
                        process.cpuPercent > 20f -> MaterialTheme.colorScheme.tertiary
                        else                     -> MaterialTheme.colorScheme.primary
                    },
                    trackColor  = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap   = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                "${"%.1f".format(process.ramMb)}MB",
                style      = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                modifier   = Modifier.width(60.dp),
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}