package com.adbcommand.app.presentation.ui.features.intentsender

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.domain.models.ExtraType
import com.adbcommand.app.domain.models.IntentExtra
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IntentSenderUiState(
    val action: String = "",
    val dataUri: String = "",
    val packageName: String = "",
    val className: String = "",
    val extras: List<IntentExtra> = emptyList(),
    val result: String? = null,
    val isError: Boolean = false
)

sealed class IntentSenderEvent {
    data class ActionChanged(val v: String): IntentSenderEvent()
    data class DataUriChanged(val v: String): IntentSenderEvent()
    data class PackageChanged(val v: String): IntentSenderEvent()
    data class ClassChanged(val v: String): IntentSenderEvent()
    object AddExtra: IntentSenderEvent()
    data class UpdateExtra(val extra: IntentExtra): IntentSenderEvent()
    data class RemoveExtra(val id: Long): IntentSenderEvent()
    object Fire: IntentSenderEvent()
    object ClearResult: IntentSenderEvent()
    object Reset: IntentSenderEvent()
}


@HiltViewModel
class IntentSenderViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntentSenderUiState())
    val uiState: StateFlow<IntentSenderUiState> = _uiState.asStateFlow()

    fun onEvent(event: IntentSenderEvent) {
        when (event) {
            is IntentSenderEvent.ActionChanged  -> _uiState.update { it.copy(action = event.v) }
            is IntentSenderEvent.DataUriChanged -> _uiState.update { it.copy(dataUri = event.v) }
            is IntentSenderEvent.PackageChanged -> _uiState.update { it.copy(packageName = event.v) }
            is IntentSenderEvent.ClassChanged   -> _uiState.update { it.copy(className = event.v) }
            is IntentSenderEvent.AddExtra -> _uiState.update {
                it.copy(extras = it.extras + IntentExtra())
            }
            is IntentSenderEvent.UpdateExtra    -> _uiState.update {
                it.copy(extras = it.extras.map { e -> if (e.id == event.extra.id) event.extra else e })
            }
            is IntentSenderEvent.RemoveExtra    -> _uiState.update {
                it.copy(extras = it.extras.filter { e -> e.id != event.id })
            }
            is IntentSenderEvent.Fire -> fireIntent()
            is IntentSenderEvent.ClearResult -> _uiState.update { it.copy(result = null) }
            is IntentSenderEvent.Reset -> _uiState.value = IntentSenderUiState()
        }
    }

    private fun fireIntent() {
        viewModelScope.launch {
            val state = _uiState.value
            try {
                val intent = Intent().apply {
                    if (state.action.isNotBlank()) action = state.action.trim()
                    if (state.dataUri.isNotBlank()) data = Uri.parse(state.dataUri.trim())

                    if (state.packageName.isNotBlank() && state.className.isNotBlank()) {
                        component = ComponentName(
                            state.packageName.trim(),
                            state.className.trim()
                        )
                    } else if (state.packageName.isNotBlank()) {
                        `package` = state.packageName.trim()
                    }

                    state.extras.forEach { extra ->
                        if (extra.key.isBlank()) return@forEach
                        when (extra.type) {
                            ExtraType.STRING -> putExtra(extra.key, extra.value)
                            ExtraType.INT -> putExtra(extra.key, extra.value.toIntOrNull() ?: 0)
                            ExtraType.BOOLEAN -> putExtra(extra.key, extra.value.toBooleanStrictOrNull() ?: false)
                            ExtraType.LONG -> putExtra(extra.key, extra.value.toLongOrNull() ?: 0L)
                            ExtraType.FLOAT -> putExtra(extra.key, extra.value.toFloatOrNull() ?: 0f)
                        }
                    }

                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)
                _uiState.update {
                    it.copy(result = "Intent fired successfully", isError = false)
                }
            } catch (e: Exception) {
                Log.e("IntentSender", "Fire failed", e)
                _uiState.update {
                    it.copy(result = "Error: ${e.message}", isError = true)
                }
            }
        }
    }
}