package com.adbcommand.app.presentation.ui.features.commands

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adbcommand.app.domain.models.AdbCommand
import com.adbcommand.app.domain.models.CommandCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandsScreen(
    ip: String,
    adbPort: String,
    pairingPort: String,
    pairingCode: String,
    onNavigateBack: () -> Unit,
    viewModel: CommandsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.init(ip, adbPort, pairingPort, pairingCode)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ADB Commands",
                        fontWeight = FontWeight.ExtraBold,
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query    = state.searchQuery,
                onQueryChange = { viewModel.onEvent(CommandsEvent.SearchChanged(it)) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            DeviceInfoChips(
                ip          = ip,
                adbPort     = adbPort,
                pairingPort = pairingPort,
                modifier    = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(12.dp))

            if (state.groupedCommands.isEmpty()) {
                EmptyState(query = state.searchQuery)
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(
                        start = 20.dp, end = 20.dp, bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.groupedCommands.forEach { (category, commands) ->
                        item(key = category.name) {
                            CategoryHeader(
                                category = category,
                                isExpanded = category in state.expandedCategories,
                                commandCount = commands.size,
                                onToggle = {
                                    viewModel.onEvent(CommandsEvent.ToggleCategory(category))
                                }
                            )
                        }

                        item(key = "${category.name}_commands") {
                            AnimatedVisibility(
                                visible = category in state.expandedCategories,
                                enter   = expandVertically(tween(200)) + fadeIn(tween(200)),
                                exit    = shrinkVertically(tween(200)) + fadeOut(tween(200))
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    commands.forEach { command ->
                                        CommandCard(
                                            command         = command,
                                            isCopied        = state.copiedCommandId == command.id,
                                            onCopy          = {
                                                viewModel.onEvent(CommandsEvent.CommandCopied(command.id))
                                                scope.launch {
                                                    delay(1500)
                                                    viewModel.onEvent(CommandsEvent.ClearCopied)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        placeholder   = { Text("Search commands…", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon   = {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
        },
        trailingIcon  = {
            AnimatedVisibility(visible = query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                }
            }
        },
        singleLine    = true,
        shape         = RoundedCornerShape(16.dp),
        modifier      = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction    = ImeAction.Search
        ),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedBorderColor      = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            focusedContainerColor   = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    )
}
@Composable
private fun DeviceInfoChips(
    ip: String,
    adbPort: String,
    pairingPort: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoChip(label = ip.ifBlank { "No IP" },          icon = Icons.Default.SettingsEthernet)
        InfoChip(label = "ADB :$adbPort",                 icon = Icons.Default.Code)
        if (pairingPort.isNotBlank()) {
            InfoChip(label = "Pair :$pairingPort",        icon = Icons.Default.Router)
        }
    }
}

@Composable
private fun InfoChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint     = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
@Composable
private fun CategoryHeader(
    category: CommandCategory,
    isExpanded: Boolean,
    commandCount: Int,
    onToggle: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = category.label,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary,
            modifier   = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text     = commandCount.toString(),
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector        = if (isExpanded) Icons.Default.ExpandLess
                else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier           = Modifier.size(20.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Command card ──────────────────────────────────────────────────────────────

@Composable
private fun CommandCard(
    command: AdbCommand,
    isCopied: Boolean,
    onCopy: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text       = command.title,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (command.needsInput) {
                            Spacer(Modifier.width(6.dp))
                            NeedsInputBadge()
                        }
                    }
                }

                FilledTonalIconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(command.command))
                        onCopy()
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isCopied)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        imageVector        = if (isCopied) Icons.Default.Check
                        else Icons.Default.ContentCopy,
                        contentDescription = if (isCopied) "Copied" else "Copy",
                        modifier           = Modifier.size(16.dp),
                        tint               = if (isCopied)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Command text in monospace
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text       = command.command,
                    style      = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color      = if (isCopied)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            // Hint
            if (command.hint.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text  = command.hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun NeedsInputBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Text(
            text     = "edit needed",
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            fontSize = 9.sp
        )
    }
}
@Composable
private fun EmptyState(query: String) {
    Box(
        modifier        = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint     = MaterialTheme.colorScheme.outline
            )
            Text(
                text  = "No commands match \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}