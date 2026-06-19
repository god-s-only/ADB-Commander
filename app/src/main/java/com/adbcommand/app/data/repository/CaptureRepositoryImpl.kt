package com.adbcommand.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    private var recordingFilePath: String = ""
    private var recordingStartMs: Long = 0L


    override suspend fun takeScreenshot(): Result<CapturedScreenshot> =
        withContext(Dispatchers.IO) {
            try {
                val cacheDir = context.cacheDir
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val cacheFile = File(cacheDir, "screenshot_$timestamp.png")
                val cachePath = cacheFile.absolutePath

                val result = shizuku.run("screencap -p $cachePath")

                delay(500)

                if (!cacheFile.exists() || cacheFile.length() == 0L) {
                    return@withContext takeScreenshotFallback(cacheFile, cachePath, timestamp)
                }

                val bitmap = BitmapFactory.decodeFile(cachePath)
                    ?: return@withContext Result.failure(
                        Exception("Could not decode screenshot — file may be corrupted")
                    )

                val savedPath = saveImageToPublicStorage(cacheFile, timestamp)

                Log.d(TAG, "Screenshot saved: $savedPath")

                Result.success(
                    CapturedScreenshot(
                        bitmap = bitmap,
                        filePath = savedPath ?: cachePath,
                        sizeBytes = cacheFile.length()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "takeScreenshot failed", e)
                Result.failure(Exception("Screenshot failed: ${e.message}"))
            }
        }

    private suspend fun takeScreenshotFallback(
        cacheFile: File,
        cachePath: String,
        timestamp: String
    ): Result<CapturedScreenshot> {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("screencap", "-p", cachePath))
            process.waitFor()
            delay(300)

            if (!cacheFile.exists() || cacheFile.length() == 0L) {
                return Result.failure(Exception("Screenshot failed: device did not produce output file"))
            }

            val bitmap = BitmapFactory.decodeFile(cachePath)
                ?: return Result.failure(Exception("Could not decode screenshot file"))

            val savedPath = saveImageToPublicStorage(cacheFile, timestamp)

            Result.success(
                CapturedScreenshot(
                    bitmap = bitmap,
                    filePath = savedPath ?: cachePath,
                    sizeBytes = cacheFile.length()
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Screenshot fallback failed: ${e.message}"))
        }
    }

    private fun saveImageToPublicStorage(file: File, timestamp: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "screenshot_$timestamp.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/AdbCommander")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                ) ?: return null

                context.contentResolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { it.copyTo(out) }
                }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
                uri.toString()
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "AdbCommander"
                )
                dir.mkdirs()
                val dest = File(dir, "screenshot_$timestamp.png")
                file.copyTo(dest, overwrite = true)
                dest.absolutePath
            }
        } catch (e: Exception) {
            Log.w(TAG, "saveImageToPublicStorage failed (non-critical)", e)
            null
        }
    }

    override suspend fun saveToGallery(filePath: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) return@withContext Result.failure(
                    Exception("File not found: $filePath")
                )
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val path = saveImageToPublicStorage(file, timestamp)
                    ?: return@withContext Result.failure(Exception("Failed to save to gallery"))
                Result.success(path)
            } catch (e: Exception) {
                Result.failure(Exception("Save failed: ${e.message}"))
            }
        }

    override suspend fun shareScreenshot(filePath: String): Result<Unit> =
        shareFile(filePath, "image/png", "Share Screenshot")


    override fun startRecording(): Flow<Long> = flow {
        try {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "AdbCommander"
            )
            dir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            recordingFilePath = "${dir.absolutePath}/recording_$timestamp.mp4"
            recordingStartMs = System.currentTimeMillis()

            val cmd = arrayOf(
                "screenrecord",
                "--bit-rate", "8000000",
                "--time-limit", "180",
                recordingFilePath
            )

            recordingProcess = Runtime.getRuntime().exec(cmd)

            if (recordingProcess == null) {
                throw Exception("Failed to start screenrecord")
            }

            Log.d(TAG, "Recording started: $recordingFilePath")

            val start = System.currentTimeMillis()

            while (true) {
                val process = recordingProcess ?: break
                try {
                    process.exitValue()
                    break
                } catch (e: IllegalThreadStateException) {
                    // Still running — emit elapsed time
                    emit(System.currentTimeMillis() - start)
                    delay(1000)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Recording flow error", e)
            throw e
        } finally {
            recordingProcess?.destroy()
            recordingProcess = null
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun stopRecording(): Result<RecordingSession> =
        withContext(Dispatchers.IO) {
            try {
                val durationMs = System.currentTimeMillis() - recordingStartMs
                try {
                    val pid = getPid(recordingProcess)
                    if (pid > 0) {
                        Runtime.getRuntime().exec(arrayOf("kill", "-2", pid.toString()))
                        delay(1000)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "SIGINT failed, falling back to destroy()", e)
                }

                recordingProcess?.destroy()
                recordingProcess = null

                delay(1200)

                val file = File(recordingFilePath)
                if (!file.exists() || file.length() == 0L) {
                    return@withContext Result.failure(
                        Exception("Recording file not found or empty at $recordingFilePath")
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                        put(MediaStore.Video.Media.RELATIVE_PATH,
                            "${Environment.DIRECTORY_MOVIES}/AdbCommander")
                    }
                    context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.sendBroadcast(
                        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                            data = Uri.fromFile(file)
                        }
                    )
                }

                Log.d(TAG, "Recording stopped: $recordingFilePath ($durationMs ms)")

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
                Result.failure(Exception("Stop recording failed: ${e.message}"))
            }
        }

    override suspend fun shareRecording(filePath: String): Result<Unit> =
        shareFile(filePath, "video/mp4", "Share Recording")


    private fun getPid(process: Process?): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                process?.pid() ?: -1
            } else {
                val field = process?.javaClass?.getDeclaredField("pid")
                field?.isAccessible = true
                (field?.get(process) as? Int) ?: -1
            }
        } catch (e: Exception) {
            -1
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
            Result.failure(Exception("Share failed: ${e.message}"))
        }
    }
}