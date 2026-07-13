package com.adbcommand.app.presentation.ui.features.paywall

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adbcommand.app.BuildConfig
import com.adbcommand.app.domain.models.FREE_FEATURES
import com.adbcommand.app.domain.models.Feature
import com.adbcommand.app.domain.models.UserPlan
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.rememberPaymentSheet

private const val STRIPE_PUBLISHABLE_KEY = BuildConfig.STRIPE_PUBLISHABLE_KEY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onNavigateBack: () -> Unit,
    highlightFeature: Feature? = null,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val entitlement by viewModel.entitlement.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        PaymentConfiguration.init(context, STRIPE_PUBLISHABLE_KEY)
    }

    val paymentSheet = rememberPaymentSheet { result ->
        viewModel.onEvent(PaywallEvent.HandlePaymentResult(result))
    }

    LaunchedEffect(state.readyToPresent) {
        if (state.readyToPresent && state.paymentIntent != null) {
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = state.paymentIntent!!.clientSecret,
                configuration = PaymentSheet.Configuration(merchantDisplayName = "ADB Commander")
            )
        }
    }

    LaunchedEffect(entitlement.plan) {
        if (entitlement.plan == UserPlan.PRO) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upgrade to Pro", fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(.4f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Star, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("ADB Commander Pro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
                        Text(
                            "Unlock full shell-level control of your Android device without a laptop.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(visible = state.errorMessage != null) {
                    state.errorMessage?.let { err ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(Modifier.width(10.dp))
                                Text(err, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.onEvent(PaywallEvent.DismissError) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = state.isVerifying) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Verifying payment…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            item {
                Text("Everything in Pro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            items(state.proFeatures) { feature ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (feature == highlightFeature) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                    )
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(feature.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(feature.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (feature == highlightFeature) {
                            Spacer(Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(50.dp), color = MaterialTheme.colorScheme.primary) {
                                Text("This one", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (state.isLoadingIntent) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text(state.paymentIntent?.formattedAmount ?: "—", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("one-time purchase · no subscription", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(.7f), textAlign = TextAlign.Center)
                            Text("Cards, bank transfer & mobile money accepted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(.6f), textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.onEvent(PaywallEvent.PresentPaymentSheet) },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !state.isLoadingIntent && !state.isVerifying && state.paymentIntent != null
                ) {
                    Icon(Icons.Default.Star, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Unlock Pro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                if (state.paymentIntent == null && !state.isLoadingIntent) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.onEvent(PaywallEvent.LoadIntent) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Retry", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.width(4.dp))
                    Text("Payments secured by Stripe", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    feature: Feature,
    onDismiss: () -> Unit,
    onNavigateToFullPaywall: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(Icons.Default.Lock, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Pro Feature", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(feature.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(feature.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(.8f))
                }
            }
            Text(
                "Upgrade to Pro to unlock this and all other premium features with a single one-time purchase.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
            Button(
                onClick = { onDismiss(); onNavigateToFullPaywall() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Star, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("See Pro Features", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss) {
                Text("Maybe later", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun ProGatedButton(
    feature: Feature,
    isPro: Boolean,
    onUnlocked: () -> Unit,
    onLocked: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val isUnlocked = feature in FREE_FEATURES || isPro
    Button(
        onClick = { if (isUnlocked) onUnlocked() else onLocked() },
        modifier = modifier,
        colors = if (!isUnlocked) ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) else ButtonDefaults.buttonColors()
    ) {
        if (!isUnlocked) {
            Icon(Icons.Default.Lock, "Pro feature", Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
        }
        content()
    }
}