package com.adbcommand.app.domain.usecase.billing

import com.adbcommand.app.core.UserIdManager
import com.adbcommand.app.domain.repository.BillingRepository
import com.adbcommand.app.domain.repository.PaymentIntentResult
import javax.inject.Inject

class CreatePaymentIntentUseCase @Inject constructor(
    private val repository: BillingRepository,
    private val userIdManager: UserIdManager
) {
    suspend operator fun invoke(): Result<PaymentIntentResult> {
        val userId = userIdManager.getUserId()
        return repository.createPaymentIntent(userId)
    }
}