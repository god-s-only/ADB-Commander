package com.adbcommand.app.domain.models

enum class UserPlan {
    FREE,
    PRO
}
data class UserEntitlement(
    val plan: UserPlan         = UserPlan.FREE,
    val purchaseToken: String? = null,
    val expiryTimeMs: Long? = null
) {
    val isPro: Boolean get() = plan == UserPlan.PRO
}
