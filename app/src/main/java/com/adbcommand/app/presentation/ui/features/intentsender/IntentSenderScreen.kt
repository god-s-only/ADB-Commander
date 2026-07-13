package com.adbcommand.app.presentation.ui.features.intentsender

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adbcommand.app.domain.models.ExtraType
import com.adbcommand.app.domain.models.IntentExtra

private val QUICK_ACTIONS = listOf(
    "android.intent.action.VIEW",
    "android.intent.action.SEND",
    "android.intent.action.DIAL",
    "android.intent.action.CALL",
    "android.intent.action.SENDTO",
    "android.intent.action.EDIT",
    "android.intent.action.SEARCH",
    "android.settings.SETTINGS",
    "android.settings.WIRELESS_SETTINGS",
    "android.settings.DEVELOPER_SETTINGS",
    "android.settings.APPLICATION_DETAILS_SETTINGS",
    "android.media.action.IMAGE_CAPTURE"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntentSenderScreen(
    onNavigateBack: () -> Unit,
    viewModel: IntentSenderViewModel = hiltViewModel()
) {
    val state        by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost  = remember { SnackbarHostState() }

    LaunchedEffect(state.result) {
        state.result?.let {
            snackbarHost.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.onEvent(IntentSenderEvent.ClearResult)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Intent Sender", fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(IntentSenderEvent.Reset) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
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

            // ── Action ────────────────────────────────────────────────────────
            SectionLabel("Action")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IntentTextField(
                        value         = state.action,
                        onValueChange = { viewModel.onEvent(IntentSenderEvent.ActionChanged(it)) },
                        label         = "Action",
                        placeholder   = "android.intent.action.VIEW"
                    )

                    // Quick-pick chips
                    Text("Quick pick", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(QUICK_ACTIONS.size) { i ->
                            val a = QUICK_ACTIONS[i]
                            FilterChip(
                                selected = state.action == a,
                                onClick  = { viewModel.onEvent(IntentSenderEvent.ActionChanged(a)) },
                                label    = {
                                    Text(
                                        a.substringAfterLast("."),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // ── Data URI ──────────────────────────────────────────────────────
            SectionLabel("Data URI (optional)")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    IntentTextField(
                        value         = state.dataUri,
                        onValueChange = { viewModel.onEvent(IntentSenderEvent.DataUriChanged(it)) },
                        label         = "Data URI",
                        placeholder   = "https://example.com  or  myapp://path"
                    )
                }
            }

            // ── Component ─────────────────────────────────────────────────────
            SectionLabel("Target Component (optional)")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IntentTextField(
                        value         = state.packageName,
                        onValueChange = { viewModel.onEvent(IntentSenderEvent.PackageChanged(it)) },
                        label         = "Package",
                        placeholder   = "com.example.app"
                    )
                    IntentTextField(
                        value         = state.className,
                        onValueChange = { viewModel.onEvent(IntentSenderEvent.ClassChanged(it)) },
                        label         = "Class (explicit only)",
                        placeholder   = "com.example.app.MainActivity"
                    )
                }
            }

            // ── Extras ────────────────────────────────────────────────────────
            SectionLabel("Extras (optional)")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.extras.forEach { extra ->
                        ExtraRow(
                            extra    = extra,
                            onChange = { viewModel.onEvent(IntentSenderEvent.UpdateExtra(it)) },
                            onRemove = { viewModel.onEvent(IntentSenderEvent.RemoveExtra(extra.id)) }
                        )
                    }

                    OutlinedButton(
                        onClick  = { viewModel.onEvent(IntentSenderEvent.AddExtra) },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Extra")
                    }
                }
            }

            // ── Fire button ───────────────────────────────────────────────────
            Button(
                onClick  = { viewModel.onEvent(IntentSenderEvent.Fire) },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape    = RoundedCornerShape(16.dp),
                enabled  = state.action.isNotBlank() || state.dataUri.isNotBlank() ||
                        state.packageName.isNotBlank()
            ) {
                Icon(Icons.Default.Send, null, Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Fire Intent", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // ── Result ────────────────────────────────────────────────────────
            AnimatedVisibility(visible = state.isError && state.result != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null, Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.width(10.dp))
                        Text(state.result ?: "", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun IntentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        placeholder   = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
        singleLine    = true,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExtraRow(
    extra: IntentExtra,
    onChange: (IntentExtra) -> Unit,
    onRemove: () -> Unit
) {
    var typeExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value         = extra.key,
                onValueChange = { onChange(extra.copy(key = it)) },
                label         = { Text("Key") },
                singleLine    = true,
                modifier      = Modifier.weight(1f),
                shape         = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
                value         = extra.value,
                onValueChange = { onChange(extra.copy(value = it)) },
                label         = { Text("Value") },
                singleLine    = true,
                modifier      = Modifier.weight(1f),
                shape         = RoundedCornerShape(10.dp)
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Close, "Remove extra",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
        ExposedDropdownMenuBox(
            expanded         = typeExpanded,
            onExpandedChange = { typeExpanded = it }
        ) {
            OutlinedTextField(
                value         = extra.type.label,
                onValueChange = {},
                readOnly      = true,
                label         = { Text("Type") },
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                singleLine    = true,
                modifier      = Modifier.menuAnchor().fillMaxWidth(),
                shape         = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(
                expanded         = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                ExtraType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.label) },
                        onClick = {
                            onChange(extra.copy(type = type))
                            typeExpanded = false
                        }
                    )
                }
            }
        }
    }
}