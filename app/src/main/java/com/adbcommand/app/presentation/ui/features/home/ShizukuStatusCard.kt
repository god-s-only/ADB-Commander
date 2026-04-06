package com.adbcommand.app.presentation.ui.features.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShizukuStatusCard(
    state: ShizukuState,
    onRequestPermission: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val containerColor = when {
        state.isFullyAvailable -> MaterialTheme.colorScheme.primaryContainer
        state.isRunning -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when {
        state.isFullyAvailable -> MaterialTheme.colorScheme.onPrimaryContainer
        state.isRunning -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }
    val icon = when {
        state.isFullyAvailable -> Icons.Default.CheckCircle
        state.isRunning -> Icons.Default.Lock
        else  -> Icons.Default.ErrorOutline
    }
    val title = when {
        state.isFullyAvailable -> "Shizuku Active"
        state.isRunning -> "Shizuku — Permission Needed"
        else -> "Shizuku Not Running"
    }
    val subtitle = when {
        state.isFullyAvailable ->
            "Privileged shell access enabled — all features unlocked"
        state.isRunning ->
            "Shizuku is running but permission hasn't been granted yet"
        else ->
            "Install and start Shizuku to enable force-stop, pm clear, and auto-read of pairing info"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint     = contentColor,
                modifier = Modifier
                    .size(22.dp)
                    .padding(top = 2.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = contentColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f),
                    lineHeight = 16.sp
                )

                AnimatedVisibility(visible = state.isRunning && !state.isPermissionGranted) {
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onRequestPermission(HomeEvent.RequestShizukuPermission) },
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = contentColor,
                            contentColor   = containerColor
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier       = Modifier.height(34.dp)
                    ) {
                        Text(
                            "Grant Permission",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                AnimatedVisibility(visible = !state.isRunning) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick        = {
                            val url = "https://shizuku.rikka.app/download/"
                            if(url.startsWith("https://")){
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setData(Uri.parse(url))
                                context.startActivity(intent)
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier       = Modifier.height(34.dp),
                        border         = androidx.compose.foundation.BorderStroke(
                            1.dp, contentColor.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint     = contentColor
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Get Shizuku",
                            style  = MaterialTheme.typography.labelMedium,
                            color  = contentColor
                        )
                    }
                }
            }
        }
    }
}