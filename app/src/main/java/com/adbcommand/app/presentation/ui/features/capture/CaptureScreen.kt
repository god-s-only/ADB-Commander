package com.adbcommand.app.presentation.ui.features.capture

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adbcommand.app.domain.models.RecordingState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    onNavigateBack: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel()
) {
    val state        by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost  = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHost.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.onEvent(CaptureEvent.DismissMessage)
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHost.showSnackbar(it, duration = SnackbarDuration.Long)
            viewModel.onEvent(CaptureEvent.DismissError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Capture",
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Spacer(Modifier.height(4.dp))

            SectionLabel("Screenshot")

            ScreenshotCard(
                state   = state,
                onEvent = viewModel::onEvent
            )

            SectionLabel("Screen Recording")

            RecordingCard(
                state   = state,
                onEvent = viewModel::onEvent
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ScreenshotCard(
    state: CaptureUiState,
    onEvent: (CaptureEvent) -> Unit
) {
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

            // Preview area
            AnimatedContent(
                targetState  = state.screenshot,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label        = "screenshot_preview"
            ) { shot ->
                if (shot != null) {
                    // Screenshot preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                    ) {
                        Image(
                            bitmap             = shot.bitmap.asImageBitmap(),
                            contentDescription = "Screenshot preview",
                            contentScale       = ContentScale.Fit,
                            modifier           = Modifier.fillMaxSize()
                        )

                        IconButton(
                            onClick  = { onEvent(CaptureEvent.DismissScreenshot) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(32.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.4f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close, "Dismiss",
                                Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Screenshot, null,
                                Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "No screenshot yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = state.screenshot != null) {
                state.screenshot?.let { shot ->
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                shot.filePath.substringAfterLast("/"),
                                style      = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                formatBytes(shot.sizeBytes),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        if (state.screenshotSaved) {
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier          = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            if (state.screenshot != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick  = { onEvent(CaptureEvent.SaveScreenshot) },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = !state.screenshotSaved
                    ) {
                        Icon(Icons.Default.Save, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (state.screenshotSaved) "Saved" else "Save",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    FilledTonalButton(
                        onClick  = { onEvent(CaptureEvent.ShareScreenshot) },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Button(
                onClick  = { onEvent(CaptureEvent.TakeScreenshot) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                enabled  = !state.isTakingScreenshot
            ) {
                AnimatedContent(
                    targetState = state.isTakingScreenshot,
                    label       = "screenshot_btn"
                ) { loading ->
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Capturing…", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Screenshot, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (state.screenshot != null) "Take New Screenshot"
                                else "Take Screenshot",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordingCard(
    state: CaptureUiState,
    onEvent: (CaptureEvent) -> Unit
) {
    val isRecording = state.recordingState is RecordingState.Recording
    val lastSession = state.lastRecording

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (isRecording)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    RecordingIndicator(elapsedMs = state.recordingElapsedMs)
                } else if (lastSession != null && lastSession.isComplete) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Videocam, null,
                            Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Recording complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatElapsed(lastSession.durationMs),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Videocam, null,
                            Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "No recording yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            AnimatedVisibility(visible = lastSession != null && lastSession.isComplete) {
                lastSession?.let { session ->
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                session.filePath.substringAfterLast("/"),
                                style      = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines   = 1
                            )
                            val size = File(session.filePath).let {
                                if (it.exists()) formatBytes(it.length()) else "—"
                            }
                            Text(
                                size,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = { onEvent(CaptureEvent.ShareRecording(session.filePath)) }
                        ) {
                            Icon(Icons.Default.Share, "Share recording", Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (!isRecording) {
                Text(
                    "Max recording duration: 3 minutes (Android system limit)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }

            if (isRecording) {
                Button(
                    onClick  = { onEvent(CaptureEvent.StopRecording) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Stop, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Stop Recording", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            } else {
                Button(
                    onClick  = { onEvent(CaptureEvent.StartRecording) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.FiberManualRecord, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (lastSession != null) "Record Again" else "Start Recording",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp
                    )
                }
            }
        }
    }
}


@Composable
private fun RecordingIndicator(elapsedMs: Long) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(pulse)
                .background(MaterialTheme.colorScheme.error, CircleShape)
        )

        Text(
            "REC",
            style      = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.error,
            letterSpacing = 2.sp
        )

        Text(
            formatElapsed(elapsedMs),
            style      = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)}KB"
    else -> "${"%.1f".format(bytes / (1024.0 * 1024))}MB"
}
