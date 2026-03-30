package com.adbcommand.app.domain.models

data class ShellResult(
    val output: String,
    val error: String,
    val success: Boolean
)
