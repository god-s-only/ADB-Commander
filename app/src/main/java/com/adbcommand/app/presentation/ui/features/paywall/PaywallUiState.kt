package com.adbcommand.app.presentation.ui.features.paywall

import com.adbcommand.app.domain.models.Feature
import com.adbcommand.app.domain.models.PRO_FEATURES
import com.adbcommand.app.domain.repository.PaymentIntentResult

data class PaywallUiState(
    val isLoadingIntent: Boolean           = false,
    val isVerifying: Boolean               = false,
    val paymentIntent: PaymentIntentResult? = null,
    val proFeatures: List<Feature> = PRO_FEATURES.toList(),
    val errorMessage: String?              = null,
    val readyToPresent: Boolean            = false
)