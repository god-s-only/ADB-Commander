package com.adbcommand.app.domain.usecase.billing

import com.adbcommand.app.core.UserIdManager
import com.adbcommand.app.domain.models.UserEntitlement
import com.adbcommand.app.domain.repository.BillingRepository
import javax.inject.Inject

class CheckEntitlementUseCase @Inject constructor(
    private val repository: BillingRepository,
    private val userIdManager: UserIdManager
) {
    suspend operator fun invoke(): Result<UserEntitlement> {
        val userId = userIdManager.getUserId()
        return repository.checkEntitlement(userId)
    }
}