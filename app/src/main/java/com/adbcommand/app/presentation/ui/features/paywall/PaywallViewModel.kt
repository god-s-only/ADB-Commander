package com.adbcommand.app.presentation.ui.features.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.core.FeatureManager
import com.adbcommand.app.domain.models.UserEntitlement
import com.adbcommand.app.domain.usecase.billing.CreatePaymentIntentUseCase
import com.adbcommand.app.domain.usecase.billing.VerifyPurchaseUseCase
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val createPaymentIntent: CreatePaymentIntentUseCase,
    private val verifyPurchase: VerifyPurchaseUseCase,
    featureManager: FeatureManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    val entitlement: StateFlow<UserEntitlement> = featureManager.entitlementFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserEntitlement())

    init {
        onEvent(PaywallEvent.LoadIntent)
    }

    fun onEvent(event: PaywallEvent) {
        when (event) {
            is PaywallEvent.LoadIntent           -> loadIntent()
            is PaywallEvent.PresentPaymentSheet  ->
                _uiState.update { it.copy(readyToPresent = true) }
            is PaywallEvent.DismissError         ->
                _uiState.update { it.copy(errorMessage = null) }
            is PaywallEvent.HandlePaymentResult  ->
                handlePaymentResult(event.result)
        }
    }

    private fun loadIntent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingIntent = true, errorMessage = null) }
            createPaymentIntent().fold(
                onSuccess = { intent ->
                    _uiState.update { it.copy(isLoadingIntent = false, paymentIntent = intent) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingIntent = false,
                            errorMessage    = "Could not reach payment server: ${err.message}"
                        )
                    }
                }
            )
        }
    }

    private fun handlePaymentResult(result: PaymentSheetResult) {
        _uiState.update { it.copy(readyToPresent = false) }
        when (result) {
            is PaymentSheetResult.Completed -> {
                val intentId = _uiState.value.paymentIntent?.paymentIntentId
                    ?: run {
                        _uiState.update {
                            it.copy(errorMessage = "Payment succeeded but verification failed. Contact support.")
                        }
                        return
                    }
                verifyServerSide(intentId)
            }
            is PaymentSheetResult.Canceled  -> {  }
            is PaymentSheetResult.Failed    -> {
                _uiState.update {
                    it.copy(errorMessage = result.error.localizedMessage ?: "Payment failed. Try again.")
                }
            }
        }
    }

    private fun verifyServerSide(paymentIntentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true) }
            verifyPurchase(paymentIntentId).fold(
                onSuccess = { entitlement ->
                    _uiState.update { it.copy(isVerifying = false) }
                    if (!entitlement.isPro) {
                        _uiState.update {
                            it.copy(
                                errorMessage = "Payment received but not yet confirmed. " +
                                        "Restart the app or contact support (ref: $paymentIntentId)"
                            )
                        }
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isVerifying  = false,
                            errorMessage = "Verification error: ${err.message}. " +
                                    "Your payment was received — restart to restore access."
                        )
                    }
                }
            )
        }
    }
}