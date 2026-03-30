package com.adbcommand.app.core

sealed class UIEvent {
    data class Navigate(val route: String): UIEvent()
    data class ShowSnackbar(val message: String, val action: String? = null): UIEvent()
    data object PopBackStack: UIEvent()
}
