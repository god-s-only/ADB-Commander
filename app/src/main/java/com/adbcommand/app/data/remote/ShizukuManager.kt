package com.adbcommand.app.data.remote

import android.content.pm.PackageManager
import android.util.Log
import com.adbcommand.app.domain.models.ShellResult
import com.adbcommand.app.presentation.ui.features.home.ShizukuState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShizukuManager @Inject constructor(
    private val executor: ShizukuShellExecutor
) {

    companion object {
        private const val TAG = "ShizukuManager"
        private const val PERMISSION_CODE = 1001
    }


    private val _state = MutableStateFlow(ShizukuState())
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Binder received")
        _state.value = _state.value.copy(
            isRunning           = true,
            isPermissionGranted = checkPermission()
        )
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(TAG, "Binder dead")
        _state.value = ShizukuState(isRunning = false)
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { code, result ->
            if (code == PERMISSION_CODE) {
                val granted = result == PackageManager.PERMISSION_GRANTED
                Log.d(TAG, "Permission result: granted=$granted")
                _state.value = _state.value.copy(isPermissionGranted = granted)
            }
        }

    fun initialize() {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)

        if (isBinderAlive()) {
            _state.value = ShizukuState(
                isRunning           = true,
                isPermissionGranted = checkPermission()
            )
        }
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
    }
    fun isAvailable(): Boolean = isBinderAlive() && checkPermission()

    fun checkPermission(): Boolean {
        if (!isBinderAlive()) return false
        return try {
            if (Shizuku.isPreV11()) false
            else Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermission() {
        if (!isBinderAlive()) {
            _state.value = _state.value.copy(isRunning = false)
            return
        }
        try {
            if (!Shizuku.isPreV11()) Shizuku.requestPermission(PERMISSION_CODE)
        } catch (e: Exception) {
            Log.e(TAG, "requestPermission failed", e)
        }
    }

    suspend fun run(cmd: String): ShellResult {
        if (!isAvailable()) {
            return ShellResult(
                output  = "",
                error   = "Shizuku not available or permission not granted",
                success = false
            )
        }
        return executor.run(cmd)
    }


    private fun isBinderAlive(): Boolean = try {
        Shizuku.pingBinder()
    } catch (e: Exception) {
        false
    }
}