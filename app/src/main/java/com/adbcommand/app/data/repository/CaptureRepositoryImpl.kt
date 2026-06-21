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
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
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
        private const val SDCARD_TEMP_DIR = "/sdcard/AdbCommander_tmp"
    }

    private var recordingProcess: Process? = null
    private var recordingFilePath: String = ""
    private var recordingStartMs: Long = 0L


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun takeScreenshot(): Result<CapturedScreenshot> =
        withContext(Dispatchers.IO) {
            try {
                if (!shizuku.isAvailable()) {
                    return@withContext Result.failure(
                        Exception("Shizuku is not running or permission not granted.")
                    )
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val tempPath = "$SDCARD_TEMP_DIR/screenshot_$timestamp.png"

                shizuku.run("mkdir -p $SDCARD_TEMP_DIR")

                val result = shizuku.run("screencap -p > $tempPath")

                if (!result.success) {
                    return@withContext Result.failure(
                        Exception("screencap failed: ${result.error}")
                    )
                }

                delay(1000)

                val tempFile = File(tempPath)
                if (!tempFile.exists() || tempFile.length() == 0L) {
                    val ls = shizuku.run("ls -la $SDCARD_TEMP_DIR")
                    Log.e(TAG, "Temp dir after screencap: ${ls.output} | err: ${ls.error}")
                    return@withContext Result.failure(
                        Exception("screencap produced no output file.")
                    )
                }

                val header = tempFile.inputStream().use { it.readNBytes(4) }
                Log.d(TAG, "PNG header: ${header.joinToString { "%02X".format(it) }}")

                val bitmap = BitmapFactory.decodeFile(tempPath)
                    ?: return@withContext Result.failure(
                        Exception("Screenshot file exists (${tempFile.length()} bytes) but could not be decoded. Header: ${header.joinToString { "%02X".format(it) }}")
                    )

                val savedPath = saveImageToPublicStorage(tempFile, timestamp)
                tempFile.delete()

                Result.success(
                    CapturedScreenshot(
                        bitmap = bitmap,
                        filePath = savedPath ?: tempPath,
                        sizeBytes = bitmap.byteCount.toLong()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "takeScreenshot failed", e)
                Result.failure(Exception("Screenshot failed: ${e.message}"))
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
            Log.w(TAG, "saveImageToPublicStorage failed", e)
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
        if (!shizuku.isAvailable()) {
            throw Exception("Shizuku is not running or permission not granted.")
        }

        shizuku.run("mkdir -p $SDCARD_TEMP_DIR")

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        recordingFilePath = "$SDCARD_TEMP_DIR/recording_$timestamp.mp4"
        recordingStartMs = System.currentTimeMillis()

        val cmd = "screenrecord --bit-rate 8000000 --time-limit 180 $recordingFilePath"

        var shizukuDone = false
        val recordingJob = kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            shizuku.run(cmd)
            shizukuDone = true
        }

        Log.d(TAG, "Recording started via Shizuku: $recordingFilePath")
        val start = System.currentTimeMillis()

        while (!shizukuDone) {
            emit(System.currentTimeMillis() - start)
            delay(1000)
        }

        recordingJob.join()

    }.flowOn(Dispatchers.IO)

    override suspend fun stopRecording(): Result<RecordingSession> =
        withContext(Dispatchers.IO) {
            try {
                val durationMs = System.currentTimeMillis() - recordingStartMs
                val killResult = shizuku.run("pkill -2 screenrecord")
                Log.d(TAG, "pkill result: ${killResult.output} | ${killResult.error}")
                delay(2000)

                val tempFile = File(recordingFilePath)
                if (!tempFile.exists() || tempFile.length() == 0L) {
                    val ls = shizuku.run("ls -la $SDCARD_TEMP_DIR")
                    Log.e(TAG, "Temp dir after stop: ${ls.output}")
                    return@withContext Result.failure(
                        Exception("Recording file not found or empty. Path: $recordingFilePath")
                    )
                }

                val finalPath = saveVideoToPublicStorage(tempFile)
                tempFile.delete()

                val resolvedPath = finalPath ?: recordingFilePath
                Log.d(TAG, "Recording saved: $resolvedPath ($durationMs ms)")

                Result.success(
                    RecordingSession(
                        filePath = resolvedPath,
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

    private fun saveVideoToPublicStorage(file: File): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_MOVIES}/AdbCommander")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                ) ?: return null

                context.contentResolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { it.copyTo(out) }
                }

                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
                uri.toString()
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "AdbCommander"
                )
                dir.mkdirs()
                val dest = File(dir, file.name)
                file.copyTo(dest, overwrite = true)
                @Suppress("DEPRECATION")
                context.sendBroadcast(
                    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                        data = Uri.fromFile(dest)
                    }
                )
                dest.absolutePath
            }
        } catch (e: Exception) {
            Log.w(TAG, "saveVideoToPublicStorage failed", e)
            null
        }
    }

    override suspend fun shareRecording(filePath: String): Result<Unit> =
        shareFile(filePath, "video/mp4", "Share Recording")


    private fun getPid(process: Process?): Int {
        if (process == null) return -1
        return try {
            var clazz: Class<*>? = process.javaClass
            while (clazz != null) {
                try {
                    val field = clazz.getDeclaredField("pid")
                    field.isAccessible = true
                    return (field.get(process) as? Int) ?: -1
                } catch (_: NoSuchFieldException) {
                    clazz = clazz.superclass
                }
            }
            -1
        } catch (e: Exception) {
            Log.w(TAG, "getPid failed: ${e.message}")
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

            val uri = if (filePath.startsWith("content://")) {
                Uri.parse(filePath)
            } else {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            }

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