package com.adbcommand.app.presentation.ui.features.logcat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adbcommand.app.domain.models.LogLevel
import com.adbcommand.app.domain.models.LogLine
import com.adbcommand.app.domain.models.LogcatEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogcatScreen(
    onNavigateBack: () -> Unit,
    viewModel: LogcatViewModel = hiltViewModel()
) {
    val state        by viewModel.uiState.collectAsStateWithLifecycle()
    val listState     = rememberLazyListState()
    val snackbarHost  = remember { SnackbarHostState() }

    LaunchedEffect(state.lines.size) {
        if (state.autoScroll && state.lines.isNotEmpty()) {
            listState.animateScrollToItem(state.lines.lastIndex)
        }
    }

    LaunchedEffect(state.saveResult) {
        state.saveResult?.let { msg ->
            snackbarHost.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.onEvent(LogcatEvent.DismissSaveResult)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Logcat",
                            fontWeight    = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "${state.lines.size} lines",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(LogcatEvent.ToggleAutoScroll) }) {
                        Icon(
                            imageVector = if (state.autoScroll)
                                Icons.Default.VerticalAlignBottom
                            else
                                Icons.Default.VerticalAlignCenter,
                            contentDescription = "Toggle auto-scroll",
                            tint = if (state.autoScroll)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(LogcatEvent.Save) }) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Save to file")
                        }
                    }
                    IconButton(onClick = { viewModel.onEvent(LogcatEvent.Clear) }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear logs")
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
        ) {

            FilterBar(
                filter   = state.filter,
                onEvent  = viewModel::onEvent,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            AnimatedVisibility(
                visible = state.error != null,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                state.error?.let { err ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier          = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline, null,
                                Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                err,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            val displayLines = if (state.filter.searchQuery.isBlank()) {
                state.lines
            } else {
                state.lines.filter { line ->
                    line.raw.contains(state.filter.searchQuery, ignoreCase = true)     ||
                            line.message.contains(state.filter.searchQuery, ignoreCase = true) ||
                            line.tag.contains(state.filter.searchQuery, ignoreCase = true)
                }
            }

            if (displayLines.isEmpty() && !state.isRunning) {
                Box(
                    modifier         = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Terminal, null,
                            Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap Start to begin streaming logs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    state          = listState,
                    modifier       = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        ),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        items = displayLines,
                        key   = { it.id }
                    ) { line ->
                        LogLineRow(line = line)
                    }
                }
            }

            BottomControlBar(
                isRunning = state.isRunning,
                onStart   = { viewModel.onEvent(LogcatEvent.Started) },
                onStop    = { viewModel.onEvent(LogcatEvent.Stopped) }
            )
        }
    }
}


@Composable
private fun FilterBar(
    filter: com.adbcommand.app.domain.models.LogcatFilter,
    onEvent: (LogcatEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Row(
            modifier              = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LogLevel.entries.forEach { level ->
                val selected = filter.level == level
                FilterChip(
                    selected = selected,
                    onClick  = { onEvent(LogcatEvent.LevelChanged(level)) },
                    label    = {
                        Text(
                            level.label,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize   = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = logLevelColor(level).copy(alpha = 0.2f),
                        selectedLabelColor     = logLevelColor(level)
                    )
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value           = filter.tag,
                onValueChange   = { onEvent(LogcatEvent.TagChanged(it)) },
                placeholder     = { Text("Tag", fontSize = 12.sp) },
                singleLine      = true,
                modifier        = Modifier.weight(1f),
                shape           = RoundedCornerShape(10.dp),
                textStyle       = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 13.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors          = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            )
            OutlinedTextField(
                value           = filter.searchQuery,
                onValueChange   = { onEvent(LogcatEvent.SearchChanged(it)) },
                placeholder     = { Text("Search", fontSize = 12.sp) },
                leadingIcon     = { Icon(Icons.Default.Search, null, Modifier.size(16.dp)) },
                trailingIcon    = {
                    if (filter.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onEvent(LogcatEvent.SearchChanged("")) }) {
                            Icon(Icons.Default.Close, null, Modifier.size(14.dp))
                        }
                    }
                },
                singleLine      = true,
                modifier        = Modifier.weight(1.5f),
                shape           = RoundedCornerShape(10.dp),
                textStyle       = LocalTextStyle.current.copy(fontSize = 13.sp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction    = ImeAction.Search
                ),
                colors          = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            )
        }
    }
}


@Composable
private fun LogLineRow(line: LogLine) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier         = Modifier
                .padding(top = 2.dp)
                .size(width = 18.dp, height = 18.dp)
                .background(
                    logLevelColor(line.level).copy(alpha = 0.15f),
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = line.level.label,
                fontSize   = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color      = logLevelColor(line.level)
            )
        }

        Spacer(Modifier.width(6.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (line.tag.isNotBlank() || line.timestamp.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    if (line.tag.isNotBlank()) {
                        Text(
                            text       = line.tag,
                            fontSize   = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color      = logLevelColor(line.level),
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                            modifier   = Modifier.widthIn(max = 160.dp)
                        )
                    }
                    if (line.timestamp.isNotBlank()) {
                        Text(
                            text       = line.timestamp,
                            fontSize   = 9.sp,
                            color      = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Text(
                text       = line.message.ifBlank { line.raw },
                fontSize   = 11.sp,
                fontFamily = FontFamily.Monospace,
                color      = MaterialTheme.colorScheme.onSurface,
                lineHeight = 15.sp
            )
        }
    }

    HorizontalDivider(
        thickness = 0.3.dp,
        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
    )
}

// ── Bottom control bar ────────────────────────────────────────────────────────

@Composable
private fun BottomControlBar(
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isRunning) {
                Row(
                    modifier          = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Streaming…",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Button(
                    onClick  = onStop,
                    modifier = Modifier.height(44.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Stop, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Stop", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick  = onStart,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Start Logcat",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun logLevelColor(level: LogLevel): Color = when (level) {
    LogLevel.VERBOSE -> MaterialTheme.colorScheme.outline
    LogLevel.DEBUG   -> MaterialTheme.colorScheme.tertiary
    LogLevel.INFO    -> MaterialTheme.colorScheme.primary
    LogLevel.WARNING -> Color(0xFFF59E0B)
    LogLevel.ERROR   -> MaterialTheme.colorScheme.error
    LogLevel.FATAL   -> Color(0xFFDC2626)
    LogLevel.SILENT  -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
}