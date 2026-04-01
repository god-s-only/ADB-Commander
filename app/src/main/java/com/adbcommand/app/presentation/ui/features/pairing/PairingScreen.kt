package com.adbcommand.app.presentation.ui.features.pairing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Cable
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adbcommand.app.presentation.pairing.PairingStep
import com.adbcommand.app.presentation.pairing.PairingViewModel


private val BgDeep    = Color(0xFF0A0D12)
private val BgCard    = Color(0xFF111620)
private val BgInput   = Color(0xFF161C27)
private val Accent    = Color(0xFF00E5FF)
private val AccentDim = Color(0xFF00B8CC)
private val Success   = Color(0xFF00E676)
private val Danger    = Color(0xFFFF5252)
private val TextPri   = Color(0xFFE8EEF7)
private val TextSec   = Color(0xFF6B7A99)
private val Border    = Color(0xFF1E2840)
private val BorderFoc = Color(0xFF00E5FF)


@Composable
fun PairingScreen(
    viewModel: PairingViewModel = hiltViewModel(),
    onPaired: (address: String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.step) {
        if (state.step == PairingStep.SUCCESS) onPaired(state.connectedAddress)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush  = Brush.radialGradient(
                            colors = listOf(Accent.copy(alpha = 0.06f), Color.Transparent),
                            center = Offset(size.width / 2f, size.height * 0.35f),
                            radius = size.width * 0.75f
                        ),
                        radius = size.width * 0.75f,
                        center = Offset(size.width / 2f, size.height * 0.35f)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PairingHeader()

            Spacer(Modifier.height(32.dp))

            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    fadeIn(tween(300)) + slideInVertically { it / 10 } togetherWith
                            fadeOut(tween(200))
                },
                label = "pairing_step_content"
            ) { step ->
                when (step) {
                    PairingStep.IDLE, PairingStep.ERROR -> PairingForm(
                        state   = state,
                        onEvent = viewModel::onEvent
                    )
                    PairingStep.PAIRING, PairingStep.CONNECTING -> PairingProgress(step)
                    PairingStep.SUCCESS -> PairingSuccess(
                        address = state.connectedAddress,
                        onReset = { viewModel.onEvent(PairingEvent.Reset) }
                    )
                }
            }
        }
    }
}
@Composable
private fun PairingHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(listOf(Accent.copy(.15f), Accent.copy(.05f)))
                )
                .border(1.dp, Accent.copy(.25f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.Wifi,
                contentDescription = null,
                tint               = Accent,
                modifier           = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text       = "Wireless Pairing",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color      = TextPri,
            letterSpacing = 0.5.sp
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text      = "Connect via Settings › Developer Options › Wireless Debugging",
            fontSize  = 12.sp,
            color     = TextSec,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun PairingForm(
    state: PairingUiState,
    onEvent: (PairingEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HowToCard()

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdbTextField(
                modifier     = Modifier.weight(1.6f),
                value        = state.ipAddress,
                onValueChange = { onEvent(PairingEvent.IpChanged(it)) },
                label        = "IP Address",
                placeholder  = "192.168.1.42",
                error        = state.ipError,
                keyboardType = KeyboardType.Decimal,
                imeAction    = ImeAction.Next,
                onNext       = { focusManager.moveFocus(FocusDirection.Right) }
            )
            AdbTextField(
                modifier     = Modifier.weight(1f),
                value        = state.port,
                onValueChange = { onEvent(PairingEvent.PortChanged(it)) },
                label        = "Port",
                placeholder  = "34567",
                error        = state.portError,
                keyboardType = KeyboardType.Number,
                imeAction    = ImeAction.Next,
                onNext       = { focusManager.moveFocus(FocusDirection.Down) }
            )
        }

        CodeInput(
            value    = state.pairingCode,
            error    = state.codeError,
            onChange = { onEvent(PairingEvent.CodeChanged(it)) },
            onDone   = {
                focusManager.clearFocus()
                onEvent(PairingEvent.StartPairing)
            }
        )


        AnimatedVisibility(visible = state.step == PairingStep.ERROR) {
            ErrorBanner(message = state.errorMessage) {
                onEvent(PairingEvent.Reset)
            }
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick  = { onEvent(PairingEvent.StartPairing) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape  = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent)
        ) {
            Text(
                text       = "Pair Device",
                color      = Color(0xFF000D12),
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}


@Composable
private fun HowToCard() {
    val steps = listOf(
        Icons.Outlined.PhoneAndroid to "Settings › Developer Options › Wireless Debugging",
        Icons.Outlined.Wifi         to "Tap \"Pair device with pairing code\"",
        Icons.Outlined.Cable        to "Enter the IP, port and 6-digit code below"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "How to pair",
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color      = Accent,
                letterSpacing = 1.2.sp
            )
            steps.forEachIndexed { i, (icon, text) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Accent.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text     = "${i + 1}. $text",
                        color    = TextSec,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AdbTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onNext: () -> Unit = {}
) {
    Column(modifier = modifier) {
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color      = if (error != null) Danger else TextSec,
            letterSpacing = 0.8.sp,
            modifier   = Modifier.padding(bottom = 6.dp)
        )

        val borderColor = when {
            error != null -> Danger
            else          -> Border
        }

        OutlinedTextField(
            value          = value,
            onValueChange  = onValueChange,
            placeholder    = { Text(placeholder, color = TextSec.copy(.5f), fontFamily = FontFamily.Monospace, fontSize = 13.sp) },
            singleLine     = true,
            modifier       = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            shape          = RoundedCornerShape(12.dp),
            colors         = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor  = borderColor,
                focusedBorderColor    = if (error != null) Danger else BorderFoc,
                unfocusedContainerColor = BgInput,
                focusedContainerColor   = BgInput,
                cursorColor             = Accent,
                focusedTextColor        = TextPri,
                unfocusedTextColor      = TextPri
            ),
            textStyle      = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontSize   = 14.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onNext = { onNext() },
                onDone = { onNext() }
            )
        )

        AnimatedVisibility(visible = error != null) {
            Text(
                text     = error ?: "",
                fontSize = 11.sp,
                color    = Danger,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun CodeInput(
    value: String,
    error: String?,
    onChange: (String) -> Unit,
    onDone: () -> Unit
) {
    Column {
        Text(
            text       = "Pairing Code",
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color      = if (error != null) Danger else TextSec,
            letterSpacing = 0.8.sp,
            modifier   = Modifier.padding(bottom = 10.dp)
        )

        Box {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0..5) {
                    val char      = value.getOrNull(i)
                    val isFilled  = char != null
                    val isCurrent = i == value.length && value.length < 6
                    val borderCol = when {
                        error != null -> Danger
                        isCurrent     -> Accent
                        isFilled      -> Accent.copy(.4f)
                        else          -> Border
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgInput)
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = borderCol,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (char != null) {
                            Text(
                                text       = char.toString(),
                                fontSize   = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color      = TextPri
                            )
                        } else if (isCurrent) {
                            val blink by rememberInfiniteTransition(label = "blink").animateFloat(
                                initialValue = 1f, targetValue = 0f,
                                animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                                label = "cursor"
                            )
                            Box(
                                modifier = Modifier
                                    .size(2.dp, 20.dp)
                                    .alpha(blink)
                                    .background(Accent, RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value          = value,
                onValueChange  = onChange,
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .alpha(0.01f),
                singleLine     = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor   = Color.Transparent
                )
            )
        }

        AnimatedVisibility(visible = error != null) {
            Text(
                text     = error ?: "",
                fontSize = 11.sp,
                color    = Danger,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun PairingProgress(step: PairingStep) {
    val stages = listOf(
        "Opening TLS Socket"    to (step == PairingStep.PAIRING || step == PairingStep.CONNECTING),
        "SPAKE2+ Handshake"     to (step == PairingStep.PAIRING || step == PairingStep.CONNECTING),
        "Certificate Exchange"  to (step == PairingStep.CONNECTING),
        "ADB Connect"           to (step == PairingStep.CONNECTING),
    )

    Column(
        modifier              = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .border(1.dp, Border, RoundedCornerShape(20.dp))
            .padding(28.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(24.dp)
    ) {
        val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 0.6f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "pulseAlpha"
        )

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.08f * pulse))
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.12f))
                    .border(2.dp, Accent.copy(.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier  = Modifier.size(28.dp),
                    color     = Accent,
                    strokeWidth = 2.5.dp,
                    strokeCap = StrokeCap.Round
                )
            }
        }

        Text(
            text       = if (step == PairingStep.PAIRING) "Pairing…" else "Connecting…",
            fontSize   = 18.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color      = TextPri
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            stages.forEach { (label, active) ->
                StageRow(label = label, active = active)
            }
        }
    }
}

@Composable
private fun StageRow(label: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (active) Accent else TextSec.copy(.3f))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text   = label,
            color  = if (active) TextPri else TextSec,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
        if (active) {
            Spacer(Modifier.width(6.dp))
            val scan by rememberInfiniteTransition(label = "scan").animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                label = "scanAlpha"
            )
            Text("…", color = Accent.copy(scan), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PairingSuccess(address: String, onReset: () -> Unit) {
    Column(
        modifier              = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .border(1.dp, Success.copy(.25f), RoundedCornerShape(20.dp))
            .padding(32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(20.dp)
    ) {
        Icon(
            imageVector        = Icons.Filled.CheckCircle,
            contentDescription = "Success",
            tint               = Success,
            modifier           = Modifier.size(56.dp)
        )

        Text(
            "Device Paired",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color      = TextPri
        )

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Success.copy(.08f),
            modifier = Modifier.border(1.dp, Success.copy(.2f), RoundedCornerShape(10.dp))
        ) {
            Text(
                text       = address,
                fontSize   = 14.sp,
                fontFamily = FontFamily.Monospace,
                color      = Success,
                modifier   = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        Text(
            text      = "Your RSA key has been saved on the device.\nYou can now send ADB commands wirelessly.",
            fontSize  = 12.sp,
            color     = TextSec,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        OutlinedButton(
            onClick  = onReset,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            border   = BorderStroke(1.dp, Border)
        ) {
            Text("Pair Another Device", color = TextSec, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = Danger.copy(.08f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Danger.copy(.3f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector        = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint               = Danger,
                modifier           = Modifier.size(18.dp).padding(top = 1.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text      = message,
                color     = Danger,
                fontSize  = 13.sp,
                lineHeight = 19.sp,
                modifier  = Modifier.weight(1f)
            )
        }
    }
}