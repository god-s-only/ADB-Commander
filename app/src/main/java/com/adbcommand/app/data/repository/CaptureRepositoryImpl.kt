package com.adbcommand.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.adbcommand.app.core.CaptureCommands
import com.adbcommand.app.data.remote.ShizukuManager
import com.adbcommand.app.domain.models.CapturedScreenshot
import com.adbcommand.app.domain.models.RecordingSession
import com.adbcommand.app.domain.repository.CaptureRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaptureRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shizuku: ShizukuManager
) : CaptureRepository {

    companion object {
        private const val TAG = "CaptureRepository"
    }
    private var recordingProcess: Process? = null
    private var recordingFilePath: String  = ""
    private var recordingStartMs: Long     = 0L

    override suspend fun takeScreenshot(): Result<CapturedScreenshot> =
        withContext(Dispatchers.IO) {
            try {
                shizuku.run(CaptureCommands.mkdirCapture())
                val filePath = CaptureCommands.newScreenshotPath()
                val result = shizuku.run(CaptureCommands.takeScreenshot(filePath))

                if (!result.success && result.error.isNotBlank()) {
                    return@withContext Result.failure(
                        Exception("screencap failed: ${result.error}")
                    )
                }

                delay(300)

                val file = File(filePath)
                if (!file.exists()) {
                    return@withContext Result.failure(
                        Exception("Screenshot file not found at $filePath")
                    )
                }

                val bitmap = BitmapFactory.decodeFile(filePath)
                    ?: return@withContext Result.failure(
                        Exception("Could not decode screenshot file")
                    )
                scanFileToMediaStore(file, "image/png")

                Log.d(TAG, "Screenshot saved: $filePath (${file.length()} bytes)")

                Result.success(
                    CapturedScreenshot(
                        bitmap = bitmap,
                        filePath = filePath,
                        sizeBytes = file.length()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "takeScreenshot failed", e)
                Result.failure(e)
            }
        }

    override suspend fun shareScreenshot(filePath: String): Result<Unit> =
        shareFile(filePath, "image/png", "Share Screenshot")

    override suspend fun saveToGallery(filePath: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) return@withContext Result.failure(
                    Exception("File not found: $filePath")
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH,
                            "${Environment.DIRECTORY_PICTURES}/AdbCommander")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }

                    val uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                    ) ?: return@withContext Result.failure(
                        Exception("MediaStore insert failed")
                    )

                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        file.inputStream().use { it.copyTo(out) }
                    }

                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)

                    Result.success(uri.toString())
                } else {
                    scanFileToMediaStore(file, "image/png")
                    Result.success(filePath)
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveToGallery failed", e)
                Result.failure(e)
            }
        }

    override fun startRecording(): Flow<Long> = flow {
        try {
            shizuku.run(CaptureCommands.mkdirCapture())

            recordingFilePath = CaptureCommands.newRecordingPath()
            recordingStartMs  = System.currentTimeMillis()

            recordingProcess = startRecordingProcess(
                CaptureCommands.startRecording(recordingFilePath)
            )

            if (recordingProcess == null) {
                throw Exception("Failed to start screenrecord process")
            }

            Log.d(TAG, "Recording started: $recordingFilePath")

            val start = System.currentTimeMillis()
            while (recordingProcess?.isAlive == true) {
                emit(System.currentTimeMillis() - start)
                delay(1000)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Recording flow error", e)
            throw e
        } finally {
            recordingProcess?.destroy()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun stopRecording(): Result<RecordingSession> =
        withContext(Dispatchers.IO) {
            try {
                val durationMs = System.currentTimeMillis() - recordingStartMs

                recordingProcess?.destroy()
                recordingProcess = null

                delay(800)

                val file = File(recordingFilePath)
                if (!file.exists()) {
                    return@withContext Result.failure(
                        Exception("Recording file not found at $recordingFilePath")
                    )
                }

                scanFileToMediaStore(file, "video/mp4")

                Log.d(TAG, "Recording stopped: $recordingFilePath (${durationMs}ms)")

                Result.success(
                    RecordingSession(
                        filePath = recordingFilePath,
                        startedAtMs = recordingStartMs,
                        isComplete = true,
                        durationMs = durationMs
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "stopRecording failed", e)
                Result.failure(e)
            }
        }

    override suspend fun shareRecording(filePath: String): Result<Unit> =
        shareFile(filePath, "video/mp4", "Share Recording")

    private fun startRecordingProcess(cmd: String): Process? {
        return try {
            val clazz  = Class.forName("rikka.shizuku.Shizuku")
            val method = clazz.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            method.invoke(null, arrayOf("sh", "-c", cmd), null, null) as? Process
        } catch (e: Exception) {
            Log.e(TAG, "startRecordingProcess via Shizuku failed", e)
            // Fallback to Runtime.exec
            try {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            } catch (ex: Exception) {
                Log.e(TAG, "Runtime.exec fallback also failed", ex)
                null
            }
        }
    }

    private fun scanFileToMediaStore(file: File, mimeType: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return
            }
            @Suppress("DEPRECATION")
            context.sendBroadcast(
                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                    data = Uri.fromFile(file)
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "MediaStore scan failed (non-critical)", e)
        }
    }

    private suspend fun shareFile(
        filePath: String,
        mimeType: String,
        title: String
    ): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext Result.failure(
                Exception("File not found: $filePath")
            )

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "shareFile failed", e)
            Result.failure(e)
        }
    }
}