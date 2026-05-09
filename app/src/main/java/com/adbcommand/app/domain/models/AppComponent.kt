package com.adbcommand.app.domain.models

data class AppComponent(
    val name: String,
    val shortName: String,
    val isExported: Boolean,
    val hasIntentFilter: Boolean
)