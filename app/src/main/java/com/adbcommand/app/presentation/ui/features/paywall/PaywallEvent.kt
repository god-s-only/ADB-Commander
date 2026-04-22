package com.adbcommand.app.presentation.ui.features.paywall

import com.stripe.android.paymentsheet.PaymentSheetResult

sealed class PaywallEvent {
    object LoadIntent: PaywallEvent()
    object PresentPaymentSheet: PaywallEvent()
    object DismissError: PaywallEvent()
    data class HandlePaymentResult(
        val result: PaymentSheetResult
    ) : PaywallEvent()
}