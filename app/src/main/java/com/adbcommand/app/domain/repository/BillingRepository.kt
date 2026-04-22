package com.adbcommand.app.domain.repository

import com.adbcommand.app.domain.models.UserEntitlement
import kotlinx.coroutines.flow.Flow

interface BillingRepository {

    val entitlement: Flow<UserEntitlement>

    suspend fun createPaymentIntent(userId: String): Result<PaymentIntentResult>

    suspend fun verifyPurchase(
        paymentIntentId: String,
        userId: String
    ): Result<UserEntitlement>

    suspend fun checkEntitlement(userId: String): Result<UserEntitlement>

    suspend fun saveEntitlement(entitlement: UserEntitlement)

    suspend fun clearEntitlement()
}

data class PaymentIntentResult(
    val clientSecret: String,
    val paymentIntentId: String,
    val formattedAmount: String
)