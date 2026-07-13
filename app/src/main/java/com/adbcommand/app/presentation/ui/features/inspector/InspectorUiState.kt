package com.adbcommand.app.presentation.ui.features.inspector

import com.adbcommand.app.domain.models.AppInspection

data class InspectorUiState(
    val isLoading: Boolean = false,
    val inspection: AppInspection? = null,
    val error: String? = null,
    val expandedSection: InspectorSection? = InspectorSection.IDENTITY
)

enum class InspectorSection(val label: String) {
    IDENTITY("Identity"),
    BUILD("Build Info"),
    STORAGE("Storage & APK"),
    SIGNING("Signing Certificate"),
    ACTIVITIES("Activities"),
    SERVICES("Services"),
    RECEIVERS("Broadcast Receivers"),
    PROVIDERS("Content Providers"),
    PERMISSIONS("Permissions")
}