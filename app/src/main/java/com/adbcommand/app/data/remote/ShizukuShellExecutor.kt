package com.adbcommand.app.data.remote

import android.util.Log
import com.adbcommand.app.domain.models.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.ShizukuRemoteProcess
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShizukuShellExecutor @Inject constructor() {

    companion object {
        private const val TAG = "ShizukuShellExecutor"
    }
    suspend fun run(cmd: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            val process = newProcessViaReflection(arrayOf("sh", "-c", cmd))
                ?: return@withContext ShellResult(
                    output  = "",
                    error   = "Shizuku newProcess returned null — is Shizuku running?",
                    success = false
                )

            val output = process.inputStream.bufferedReader().use { it.readText() }
            val error  = process.errorStream.bufferedReader().use { it.readText() }
            val exit   = process.waitFor()

            Log.d(TAG, "cmd='$cmd' exit=$exit error='$error'")

            process.destroy()

            ShellResult(
                output  = output.trim(),
                error   = error.trim(),
                success = exit == 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "run() failed for cmd='$cmd'", e)
            ShellResult(
                output  = "",
                error   = e.message ?: "Unknown Shizuku error",
                success = false
            )
        }
    }
    private fun newProcessViaReflection(cmd: Array<String>): ShizukuRemoteProcess? {
        return try {
            val clazz = Class.forName("rikka.shizuku.Shizuku")
            val method = clazz.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true

            method.invoke(null, cmd, null, null) as? ShizukuRemoteProcess
        } catch (e: NoSuchMethodException) {
            Log.e(TAG, "newProcess method not found — Shizuku may have removed it. Migrate to UserService.", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "newProcessViaReflection failed", e)
            null
        }
    }
}