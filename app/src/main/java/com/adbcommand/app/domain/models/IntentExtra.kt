package com.adbcommand.app.domain.models

data class IntentExtra(
    val id: Long = System.currentTimeMillis(),
    val key: String   = "",
    val value: String = "",
    val type: ExtraType = ExtraType.STRING
)

enum class ExtraType(val label: String) {
    STRING("String"),
    INT("Int"),
    BOOLEAN("Boolean"),
    LONG("Long"),
    FLOAT("Float")
}