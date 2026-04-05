package com.adbcommand.app.presentation.ui.features.appmanager

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adbcommand.app.domain.models.AppActionResult
import com.adbcommand.app.domain.models.AppInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppManagerViewModel = hiltViewModel()
) {
    val state        by viewModel.uiState.collectAsStateWithLifecycle()
    val scope         = rememberCoroutineScope()
    val snackbarHost  = remember { SnackbarHostState() }
    val sheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Show snackbar whenever an action result arrives
    LaunchedEffect(state.actionResult) {
        state.actionResult?.let { result ->
            val message = when (result) {
                is AppActionResult.Success -> result.message
                is AppActionResult.Failure -> "Error: ${result.message}"
            }
            snackbarHost.showSnackbar(message, duration = SnackbarDuration.Short)
            viewModel.onEvent(AppManagerEvent.DismissActionResult)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "App Manager",
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
                    // Toggle system apps
                    IconButton(onClick = { viewModel.onEvent(AppManagerEvent.ToggleSystemApps) }) {
                        Icon(
                            imageVector = if (state.includeSystem)
                                Icons.Default.PhoneAndroid
                            else
                                Icons.Default.Android,
                            contentDescription = "Toggle system apps",
                            tint = if (state.includeSystem)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Refresh
                    IconButton(onClick = { viewModel.onEvent(AppManagerEvent.LoadApps) }) {
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
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Search bar ────────────────────────────────────────────────────
            AppSearchBar(
                query         = state.searchQuery,
                onQueryChange = { viewModel.onEvent(AppManagerEvent.SearchChanged(it)) },
                modifier      = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // ── App count chip ────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text  = "${state.filteredApps.size} apps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                if (state.includeSystem) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text  = "incl. system",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Error ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = state.error != null,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                state.error?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier          = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text  = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // ── App list ──────────────────────────────────────────────────────
            if (state.isLoading && state.apps.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Loading apps…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else if (state.filteredApps.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint     = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text  = if (state.searchQuery.isNotBlank())
                                "No apps match \"${state.searchQuery}\""
                            else "No apps found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(
                        start = 20.dp, end = 20.dp, bottom = 32.dp, top = 4.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.filteredApps,
                        key   = { it.packageName }
                    ) { app ->
                        AppRow(
                            app     = app,
                            onClick = { viewModel.onEvent(AppManagerEvent.SelectApp(app)) }
                        )
                    }
                }
            }
        }
    }

    // ── Bottom sheet ──────────────────────────────────────────────────────────
    if (state.selectedApp != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(AppManagerEvent.DismissBottomSheet) },
            sheetState       = sheetState,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AppActionsSheet(
                app          = state.selectedApp!!,
                pendingAction = state.pendingAction,
                onKill       = { viewModel.onEvent(AppManagerEvent.Kill(it)) },
                onClear      = { viewModel.onEvent(AppManagerEvent.ClearData(it)) },
                onExtract    = { viewModel.onEvent(AppManagerEvent.ExtractApk(it)) },
                onUninstall  = { viewModel.onEvent(AppManagerEvent.Uninstall(it)) },
                onLaunch     = { viewModel.onEvent(AppManagerEvent.Launch(it)) },
                onDismiss    = { viewModel.onEvent(AppManagerEvent.DismissBottomSheet) }
            )
        }
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@Composable
private fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        placeholder   = { Text("Search apps or package name…") },
        leadingIcon   = {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
        },
        trailingIcon  = {
            AnimatedVisibility(visible = query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
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

// ── App row ───────────────────────────────────────────────────────────────────

@Composable
private fun AppRow(app: AppInfo, onClick: () -> Unit) {
    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            AppIcon(drawable = app.icon, modifier = Modifier.size(46.dp))

            Spacer(Modifier.width(14.dp))

            // Name + package
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = app.appName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text     = app.packageName,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text  = "v${app.versionName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint     = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ── App icon ──────────────────────────────────────────────────────────────────

@Composable
private fun AppIcon(drawable: Drawable?, modifier: Modifier = Modifier) {
    if (drawable != null) {
        val bitmap = remember(drawable) { drawable.toBitmap().asImageBitmap() }
        Image(
            bitmap             = bitmap,
            contentDescription = null,
            modifier           = modifier
        )
    } else {
        // Fallback placeholder
        Surface(
            modifier = modifier,
            shape    = RoundedCornerShape(12.dp),
            color    = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Android,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint     = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

// ── Bottom sheet actions ──────────────────────────────────────────────────────

@Composable
private fun AppActionsSheet(
    app: AppInfo,
    pendingAction: AppAction?,
    onKill: (String) -> Unit,
    onClear: (String) -> Unit,
    onExtract: (String) -> Unit,
    onUninstall: (String) -> Unit,
    onLaunch: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // App header
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(drawable = app.icon, modifier = Modifier.size(52.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = app.appName,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text  = "v${app.versionName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

        // Action buttons
        ActionButton(
            label        = "Launch App",
            icon         = Icons.Default.PlayArrow,
            isLoading    = pendingAction == AppAction.LAUNCH,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick      = { onLaunch(app.packageName) }
        )
        ActionButton(
            label        = "Kill App",
            icon         = Icons.Default.Stop,
            isLoading    = pendingAction == AppAction.KILL,
            onClick      = { onKill(app.packageName) }
        )
        ActionButton(
            label        = "Clear Data",
            icon         = Icons.Default.DeleteSweep,
            isLoading    = pendingAction == AppAction.CLEAR,
            onClick      = { onClear(app.packageName) }
        )
        ActionButton(
            label        = "Extract APK",
            icon         = Icons.Default.FolderZip,
            isLoading    = pendingAction == AppAction.EXTRACT,
            onClick      = { onExtract(app.packageName) }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

        // Destructive — uninstall
        ActionButton(
            label          = "Uninstall",
            icon           = Icons.Default.Delete,
            isLoading      = pendingAction == AppAction.UNINSTALL,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor   = MaterialTheme.colorScheme.onErrorContainer,
            onClick        = { onUninstall(app.packageName) }
        )

        Spacer(Modifier.height(4.dp))
    }
}

// ── Action button ─────────────────────────────────────────────────────────────

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    isLoading: Boolean,
    containerColor: androidx.compose.ui.graphics.Color =
        MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
    contentColor: androidx.compose.ui.graphics.Color =
        MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick      = onClick,
        modifier     = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape        = RoundedCornerShape(14.dp),
        color        = containerColor,
        enabled      = !isLoading
    ) {
        Row(
            modifier          = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color       = contentColor
                )
            } else {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint     = contentColor
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text       = label,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = contentColor
            )
        }
    }
}