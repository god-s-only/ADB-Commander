package com.adbcommand.app.core

import com.adbcommand.app.domain.models.FREE_FEATURES
import com.adbcommand.app.domain.models.Feature
import com.adbcommand.app.domain.models.PRO_FEATURES
import com.adbcommand.app.domain.models.UserEntitlement
import com.adbcommand.app.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureManager @Inject constructor(
    private val repository: BillingRepository
) {
    val isProFlow: Flow<Boolean> = repository.entitlement.map { it.isPro }

    val entitlementFlow: Flow<UserEntitlement> = repository.entitlement

    fun isUnlocked(feature: Feature, isPro: Boolean): Boolean {
        return feature in FREE_FEATURES || isPro
    }

    fun availableFeatures(isPro: Boolean): Set<Feature> {
        return if (isPro) Feature.entries.toSet() else FREE_FEATURES
    }

    fun proOnlyFeatures(): Set<Feature> = PRO_FEATURES
}