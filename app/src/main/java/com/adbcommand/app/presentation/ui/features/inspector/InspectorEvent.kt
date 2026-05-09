package com.adbcommand.app.presentation.ui.features.inspector

sealed class InspectorEvent {
    data class Load(val packageName: String) : InspectorEvent()
    data class ToggleSection(val section: InspectorSection) : InspectorEvent()
}